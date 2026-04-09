package com.mochu.system.controller;

import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.system.dto.AnnouncementCommentDTO;
import com.mochu.system.dto.AnnouncementDTO;
import com.mochu.system.dto.AnnouncementQueryDTO;
import com.mochu.system.dto.ApprovalRemarkDTO;
import com.mochu.system.service.AnnouncementService;
import com.mochu.system.vo.AnnouncementCommentVO;
import com.mochu.system.vo.AnnouncementVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理接口 — 对照 V3.2 公告模块
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ==================== 管理端接口 ====================

    /**
     * 公告列表（管理端） — GET /api/v1/admin/announcements
     */
    @GetMapping("/admin/announcements")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<PageResult<AnnouncementVO>> list(AnnouncementQueryDTO dto) {
        return R.ok(announcementService.list(dto));
    }

    /**
     * 公告详情 — GET /api/v1/admin/announcements/{id}
     */
    @GetMapping("/admin/announcements/{id}")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<AnnouncementVO> detail(@PathVariable Integer id) {
        return R.ok(announcementService.getById(id));
    }

    /**
     * 创建公告 — POST /api/v1/admin/announcements
     */
    @PostMapping("/admin/announcements")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Integer> create(@Valid @RequestBody AnnouncementDTO dto) {
        return R.ok(announcementService.create(dto));
    }

    /**
     * 更新公告 — PUT /api/v1/admin/announcements/{id}
     */
    @PutMapping("/admin/announcements/{id}")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody AnnouncementDTO dto) {
        dto.setId(id);
        announcementService.update(dto);
        return R.ok();
    }

    /**
     * 提交审批 — PATCH /api/v1/admin/announcements/{id}/submit-approval
     */
    @PatchMapping("/admin/announcements/{id}/submit-approval")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> submitApproval(@PathVariable Integer id) {
        announcementService.submitApproval(id);
        return R.ok();
    }

    /**
     * 审批通过 — PATCH /api/v1/admin/announcements/{id}/approve
     */
    @PatchMapping("/admin/announcements/{id}/approve")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> approve(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        announcementService.approve(id, dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    /**
     * 审批驳回 — PATCH /api/v1/admin/announcements/{id}/reject
     */
    @PatchMapping("/admin/announcements/{id}/reject")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> reject(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        announcementService.reject(id, dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    /**
     * 发布公告 — PATCH /api/v1/admin/announcements/{id}/publish
     */
    @PatchMapping("/admin/announcements/{id}/publish")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> publish(@PathVariable Integer id) {
        announcementService.publish(id);
        return R.ok();
    }

    /**
     * 下线公告 — PATCH /api/v1/admin/announcements/{id}/offline
     */
    @PatchMapping("/admin/announcements/{id}/offline")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> offline(@PathVariable Integer id) {
        announcementService.offline(id);
        return R.ok();
    }

    /**
     * 置顶/取消置顶 — PATCH /api/v1/admin/announcements/{id}/toggle-top
     */
    @PatchMapping("/admin/announcements/{id}/toggle-top")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> toggleTop(@PathVariable Integer id) {
        announcementService.toggleTop(id);
        return R.ok();
    }

    /**
     * 删除公告 — DELETE /api/v1/admin/announcements/{id}
     */
    @DeleteMapping("/admin/announcements/{id}")
    @PreAuthorize("hasAuthority('system:announcement-manage')")
    public R<Void> delete(@PathVariable Integer id) {
        announcementService.delete(id);
        return R.ok();
    }

    // ==================== 前台接口（所有登录用户） ====================

    /**
     * 已发布公告列表 — GET /api/v1/announcements
     */
    @GetMapping("/announcements")
    public R<List<AnnouncementVO>> listPublished(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(announcementService.listPublished(limit));
    }

    /**
     * 公告详情（前台） — GET /api/v1/announcements/{id}
     */
    @GetMapping("/announcements/{id}")
    public R<AnnouncementVO> publicDetail(@PathVariable Integer id) {
        return R.ok(announcementService.getPublishedById(id));
    }

    /**
     * 公告评论列表 — GET /api/v1/announcements/{id}/comments
     */
    @GetMapping("/announcements/{id}/comments")
    public R<List<AnnouncementCommentVO>> listComments(@PathVariable Integer id) {
        return R.ok(announcementService.listComments(id));
    }

    /**
     * 发表评论 — POST /api/v1/announcements/{id}/comments
     */
    @PostMapping("/announcements/{id}/comments")
    public R<Integer> addComment(@PathVariable Integer id, @Valid @RequestBody AnnouncementCommentDTO dto) {
        return R.ok(announcementService.addComment(id, dto));
    }

    /**
     * 删除评论 — DELETE /api/v1/announcements/comments/{commentId}
     */
    @DeleteMapping("/announcements/comments/{commentId}")
    public R<Void> deleteComment(@PathVariable Integer commentId) {
        announcementService.deleteComment(commentId);
        return R.ok();
    }
}
