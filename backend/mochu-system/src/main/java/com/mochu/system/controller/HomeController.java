package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.common.security.SecurityUtils;
import com.mochu.system.service.HomeService;
import com.mochu.system.service.UserService;
import com.mochu.system.vo.HomeVO;
import com.mochu.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 首页 + 个人信息接口
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final UserService userService;

    /**
     * 首页数据 — GET /api/v1/home
     */
    @GetMapping("/home")
    public R<HomeVO> home() {
        return R.ok(homeService.getHomeData());
    }

    /**
     * 待办数量 — GET /api/v1/home/todo-count
     */
    @GetMapping("/home/todo-count")
    public R<Integer> todoCount() {
        return R.ok(homeService.getTodoCount());
    }

    /**
     * 当前登录用户信息 — GET /api/v1/user/me
     */
    @GetMapping("/user/me")
    public R<UserVO> currentUser() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userService.getUserById(userId));
    }
}
