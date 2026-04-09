package com.mochu.system.service;

import com.mochu.common.constant.Constants;
import com.mochu.common.security.SecurityUtils;
import com.mochu.system.vo.AnnouncementVO;
import com.mochu.system.vo.HomeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AnnouncementService announcementService;
    private final TodoService todoService;

    /**
     * 获取首页数据
     */
    public HomeVO getHomeData() {
        HomeVO vo = new HomeVO();

        // 待办数量 — 优先读缓存，无缓存则查DB并回写
        vo.setTodoCount(getTodoCount());

        // 最新公告（已发布，最多5条）
        try {
            List<AnnouncementVO> published = announcementService.listPublished(5);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            List<HomeVO.AnnouncementVO> annoList = published.stream().map(a -> {
                HomeVO.AnnouncementVO avo = new HomeVO.AnnouncementVO();
                avo.setId(a.getId());
                avo.setTitle(a.getTitle());
                avo.setPublishTime(a.getPublishTime() != null ? a.getPublishTime().format(fmt) : "");
                return avo;
            }).collect(Collectors.toList());
            vo.setAnnouncements(annoList);
        } catch (Exception e) {
            log.warn("加载首页公告失败: {}", e.getMessage());
            vo.setAnnouncements(new ArrayList<>());
        }

        // 快捷入口暂返回空列表
        vo.setShortcuts(new ArrayList<>());

        return vo;
    }

    /**
     * 待办数量 — 优先Redis缓存, 缓存miss时查DB并回写
     */
    public Integer getTodoCount() {
        Integer userId = SecurityUtils.getCurrentUserId();
        String todoKey = Constants.REDIS_TODO_COUNT_PREFIX + userId;
        Object todoCount = redisTemplate.opsForValue().get(todoKey);
        if (todoCount instanceof Number n) {
            return n.intValue();
        }
        // 缓存miss — 查DB并回写
        long count = todoService.countPending();
        redisTemplate.opsForValue().set(todoKey, (int) count,
                Constants.TODO_COUNT_CACHE_SECONDS, TimeUnit.SECONDS);
        return (int) count;
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
