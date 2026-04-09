package com.mochu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.security.SecurityUtils;
import com.mochu.system.entity.SysTodo;
import com.mochu.system.mapper.SysTodoMapper;
import com.mochu.system.vo.TodoVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 待办服务 — 含优先级/催办/分类统计/批量处理/缓存同步
 */
@Service
@RequiredArgsConstructor
public class TodoService {

    private final SysTodoMapper todoMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // ===================== 查询 =====================

    /**
     * 当前用户待办列表（分页 + 多条件筛选）
     */
    public PageResult<TodoVO> listMyTodos(Integer status, String bizType, Integer priority,
                                           Integer page, Integer size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (page == null || page < 1) page = Constants.DEFAULT_PAGE;
        if (size == null || size < 1) size = Constants.DEFAULT_SIZE;

        Page<SysTodo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getUserId, userId);
        if (status != null) {
            wrapper.eq(SysTodo::getStatus, status);
        }
        if (bizType != null && !bizType.isBlank()) {
            wrapper.eq(SysTodo::getBizType, bizType);
        }
        if (priority != null) {
            wrapper.eq(SysTodo::getPriority, priority);
        }
        // 排序: 特急>紧急>普通, 然后按创建时间倒序
        wrapper.orderByDesc(SysTodo::getPriority)
               .orderByDesc(SysTodo::getCreatedAt);

        todoMapper.selectPage(pageParam, wrapper);
        List<TodoVO> voList = pageParam.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageParam.getTotal(), page, size);
    }

    /**
     * 兼容旧接口
     */
    public PageResult<TodoVO> listMyTodos(Integer status, Integer page, Integer size) {
        return listMyTodos(status, null, null, page, size);
    }

    /**
     * 当前用户待办数量
     */
    public long countPending() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return todoMapper.selectCount(
                new LambdaQueryWrapper<SysTodo>()
                        .eq(SysTodo::getUserId, userId)
                        .eq(SysTodo::getStatus, 0)
        );
    }

    /**
     * 按业务类型分组统计(当前用户待处理)
     */
    public List<TodoStatItem> statByBizType() {
        Integer userId = SecurityUtils.getCurrentUserId();
        List<SysTodo> pending = todoMapper.selectList(
                new LambdaQueryWrapper<SysTodo>()
                        .eq(SysTodo::getUserId, userId)
                        .eq(SysTodo::getStatus, 0));

        Map<String, Integer> countMap = new LinkedHashMap<>();
        int urgentCount = 0;
        int overdueCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (SysTodo t : pending) {
            countMap.merge(t.getBizType() != null ? t.getBizType() : "other", 1, Integer::sum);
            if (t.getPriority() != null && t.getPriority() >= 1) urgentCount++;
            if (t.getDeadline() != null && t.getDeadline().isBefore(now)) overdueCount++;
        }

        List<TodoStatItem> items = new ArrayList<>();
        countMap.forEach((type, count) -> {
            TodoStatItem item = new TodoStatItem();
            item.setBizType(type);
            item.setCount(count);
            items.add(item);
        });

        // 追加汇总项
        TodoStatItem total = new TodoStatItem();
        total.setBizType("_total");
        total.setCount(pending.size());
        items.add(0, total);

        TodoStatItem urgent = new TodoStatItem();
        urgent.setBizType("_urgent");
        urgent.setCount(urgentCount);
        items.add(1, urgent);

        TodoStatItem overdue = new TodoStatItem();
        overdue.setBizType("_overdue");
        overdue.setCount(overdueCount);
        items.add(2, overdue);

        return items;
    }

    // ===================== 操作 =====================

    /**
     * 标记已处理
     */
    public void markDone(Integer id) {
        SysTodo todo = todoMapper.selectById(id);
        if (todo == null) {
            throw new BusinessException(404, "待办不存在");
        }
        Integer userId = SecurityUtils.getCurrentUserId();
        if (!todo.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能处理自己的待办");
        }
        todo.setStatus(1);
        todo.setHandledAt(LocalDateTime.now());
        todo.setHandlerId(userId);
        todoMapper.updateById(todo);

        // 同步缓存
        syncTodoCountCache(userId);
    }

    /**
     * 批量标记已处理
     */
    public void batchMarkDone(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        Integer userId = SecurityUtils.getCurrentUserId();

        List<SysTodo> todos = todoMapper.selectList(
                new LambdaQueryWrapper<SysTodo>()
                        .in(SysTodo::getId, ids)
                        .eq(SysTodo::getUserId, userId)
                        .eq(SysTodo::getStatus, 0));

        LocalDateTime now = LocalDateTime.now();
        for (SysTodo todo : todos) {
            todo.setStatus(1);
            todo.setHandledAt(now);
            todo.setHandlerId(userId);
            todoMapper.updateById(todo);
        }

        syncTodoCountCache(userId);
    }

    /**
     * 催办
     */
    public void remind(Integer id) {
        SysTodo todo = todoMapper.selectById(id);
        if (todo == null) throw new BusinessException(404, "待办不存在");
        if (todo.getStatus() == 1) throw new BusinessException("该待办已处理，无需催办");

        todo.setRemindCount(todo.getRemindCount() != null ? todo.getRemindCount() + 1 : 1);
        todo.setLastRemindAt(LocalDateTime.now());

        // 催办自动升级优先级
        if (todo.getPriority() == null || todo.getPriority() < 1) {
            todo.setPriority(1); // 升级为紧急
        } else if (todo.getPriority() == 1 && todo.getRemindCount() >= 3) {
            todo.setPriority(2); // 多次催办升级为特急
        }
        todoMapper.updateById(todo);
    }

    // ===================== 创建待办(内部方法) =====================

    /**
     * 创建待办（增强版 — 含优先级/截止/跳转链接）
     */
    public void createTodo(Integer userId, String bizType, Integer bizId,
                           String title, String content,
                           Integer priority, LocalDateTime deadline, String linkUrl) {
        SysTodo todo = new SysTodo();
        todo.setUserId(userId);
        todo.setBizType(bizType);
        todo.setBizId(bizId);
        todo.setTitle(title);
        todo.setContent(content);
        todo.setStatus(0);
        todo.setPriority(priority != null ? priority : 0);
        todo.setDeadline(deadline);
        todo.setLinkUrl(linkUrl);
        todo.setRemindCount(0);
        todoMapper.insert(todo);

        // 同步缓存
        syncTodoCountCache(userId);
    }

    /**
     * 创建待办（简化版 — 兼容旧调用）
     */
    public void createTodo(Integer userId, String bizType, Integer bizId, String title, String content) {
        createTodo(userId, bizType, bizId, title, content, 0, null, null);
    }

    /**
     * 按业务类型+单据ID批量标记已处理（审批流转时调用）
     */
    public void markDoneByBiz(String bizType, Integer bizId) {
        List<SysTodo> todos = todoMapper.selectList(
                new LambdaQueryWrapper<SysTodo>()
                        .eq(SysTodo::getBizType, bizType)
                        .eq(SysTodo::getBizId, bizId)
                        .eq(SysTodo::getStatus, 0));

        Set<Integer> affectedUsers = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        for (SysTodo t : todos) {
            t.setStatus(1);
            t.setHandledAt(now);
            todoMapper.updateById(t);
            affectedUsers.add(t.getUserId());
        }
        // 同步所有受影响用户的缓存
        affectedUsers.forEach(this::syncTodoCountCache);
    }

    /**
     * 按用户+业务类型+单据ID标记已处理
     */
    public void markDoneByUserAndBiz(Integer userId, String bizType, Integer bizId) {
        List<SysTodo> todos = todoMapper.selectList(
                new LambdaQueryWrapper<SysTodo>()
                        .eq(SysTodo::getUserId, userId)
                        .eq(SysTodo::getBizType, bizType)
                        .eq(SysTodo::getBizId, bizId)
                        .eq(SysTodo::getStatus, 0));

        LocalDateTime now = LocalDateTime.now();
        for (SysTodo t : todos) {
            t.setStatus(1);
            t.setHandledAt(now);
            todoMapper.updateById(t);
        }
        syncTodoCountCache(userId);
    }

    // ===================== 缓存同步 =====================

    /**
     * 同步用户待办数量到 Redis 缓存
     */
    public void syncTodoCountCache(Integer userId) {
        long count = todoMapper.selectCount(
                new LambdaQueryWrapper<SysTodo>()
                        .eq(SysTodo::getUserId, userId)
                        .eq(SysTodo::getStatus, 0));
        String todoKey = Constants.REDIS_TODO_COUNT_PREFIX + userId;
        redisTemplate.opsForValue().set(todoKey, (int) count,
                Constants.TODO_COUNT_CACHE_SECONDS, TimeUnit.SECONDS);
    }

    // ===================== 内部方法 =====================

    private TodoVO toVO(SysTodo entity) {
        TodoVO vo = new TodoVO();
        BeanUtils.copyProperties(entity, vo);
        // 计算是否超期
        if (entity.getStatus() == 0 && entity.getDeadline() != null) {
            vo.setOverdue(entity.getDeadline().isBefore(LocalDateTime.now()));
        } else {
            vo.setOverdue(false);
        }
        return vo;
    }

    @Data
    public static class TodoStatItem {
        private String bizType;
        private Integer count;
    }
}
