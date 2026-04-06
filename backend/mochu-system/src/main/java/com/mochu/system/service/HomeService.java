package com.mochu.system.service;

import com.mochu.common.constant.Constants;
import com.mochu.common.security.SecurityUtils;
import com.mochu.system.vo.HomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 首页服务 — 对照 V3.2 §4.2, §5.9.2
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取首页数据
     */
    public HomeVO getHomeData() {
        Integer userId = SecurityUtils.getCurrentUserId();
        HomeVO vo = new HomeVO();

        // 待办数量（从 Redis 缓存读取，无缓存则返回 0）
        vo.setTodoCount(getTodoCount());

        // 公告和快捷入口暂返回空列表（业务模块完成后补充）
        vo.setAnnouncements(new ArrayList<>());
        vo.setShortcuts(new ArrayList<>());

        return vo;
    }

    /**
     * 待办数量 — V3.2 §5.9.2
     */
    public Integer getTodoCount() {
        Integer userId = SecurityUtils.getCurrentUserId();
        String todoKey = Constants.REDIS_TODO_COUNT_PREFIX + userId;
        Object todoCount = redisTemplate.opsForValue().get(todoKey);
        return todoCount instanceof Number n ? n.intValue() : 0;
    }

    /**
     * 待办列表 — V3.2 §5.9.2
     * 业务模块完成后关联 biz_approval_instance 查询
     */
    public List<Map<String, Object>> getTodoList() {
        // TODO: 关联 biz_approval_instance 查询当前用户待审批的记录
        return new ArrayList<>();
    }

    /**
     * 更新待办数量缓存
     */
    public void updateTodoCount(Integer userId, int count) {
        String todoKey = Constants.REDIS_TODO_COUNT_PREFIX + userId;
        redisTemplate.opsForValue().set(todoKey, count,
                Constants.TODO_COUNT_CACHE_SECONDS, TimeUnit.SECONDS);
    }
}
