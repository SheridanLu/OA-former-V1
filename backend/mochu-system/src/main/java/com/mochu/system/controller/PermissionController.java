package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.service.PermissionService;
import com.mochu.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限查询接口 — 对照 V3.2 §5.9.5
 */
@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 全部权限列表 — GET /api/v1/admin/permissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system:role-manage')")
    public R<List<PermissionVO>> list() {
        return R.ok(permissionService.listAll());
    }

    /**
     * 按模块查询 — GET /api/v1/admin/permissions?module=xxx
     */
    @GetMapping(params = "module")
    @PreAuthorize("hasAuthority('system:role-manage')")
    public R<List<PermissionVO>> listByModule(@RequestParam String module) {
        return R.ok(permissionService.listByModule(module));
    }
}
