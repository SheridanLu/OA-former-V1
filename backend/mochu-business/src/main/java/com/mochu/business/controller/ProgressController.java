package com.mochu.business.controller;

import com.mochu.business.dto.ApprovalRemarkDTO;
import com.mochu.business.dto.ChangeOrderDTO;
import com.mochu.business.dto.GanttTaskDTO;
import com.mochu.business.dto.MilestoneDTO;
import com.mochu.business.entity.*;
import com.mochu.business.service.*;
import com.mochu.business.vo.GanttTaskVO;
import com.mochu.business.vo.MilestoneVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 进度管理 & 变更管理接口 — WBS + 状态流转 + 依赖 + 审计 + 预警 + 统计 + 导出
 */
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final ProgressAuditService auditService;
    private final WarningService warningService;
    private final ProgressStatService statService;
    private final ProgressExportService exportService;

    // ===================== 甘特图任务 /gantt =====================

    @GetMapping("/gantt")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<PageResult<BizGanttTask>> listGanttTasks(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(progressService.listGanttTasksPaged(projectId, taskType, page, size));
    }

    @GetMapping("/gantt/tree")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<GanttTaskVO>> listGanttTree(@RequestParam(required = false) Integer projectId) {
        return R.ok(progressService.listGanttTree(projectId));
    }

    @GetMapping("/gantt/{id}")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<BizGanttTask> getGanttTask(@PathVariable Integer id) {
        BizGanttTask task = progressService.getGanttTaskById(id);
        if (task == null) return R.fail(404, "任务不存在");
        return R.ok(task);
    }

    @PostMapping("/gantt")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> createGanttTask(@Valid @RequestBody GanttTaskDTO dto) {
        progressService.createGanttTask(dto);
        return R.ok();
    }

    @PutMapping("/gantt/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateGanttTask(@PathVariable Integer id, @Valid @RequestBody GanttTaskDTO dto) {
        progressService.updateGanttTask(id, dto);
        return R.ok();
    }

    @PatchMapping("/gantt/{id}/status")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateGanttTaskStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        progressService.updateGanttTaskStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/gantt/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> deleteGanttTask(@PathVariable Integer id) {
        progressService.deleteGanttTask(id);
        return R.ok();
    }

    // ===================== 任务状态流转(细粒度 PATCH) =====================

    @PatchMapping("/gantt/{id}/start")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> startTask(@PathVariable Integer id) {
        progressService.startTask(id, SecurityUtils.getCurrentUserId());
        return R.ok();
    }

    @PatchMapping("/gantt/{id}/submit-review")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> submitForReview(@PathVariable Integer id) {
        progressService.submitForReview(id, SecurityUtils.getCurrentUserId());
        return R.ok();
    }

    @PatchMapping("/gantt/{id}/approve")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> approveTask(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        progressService.approveTask(id, SecurityUtils.getCurrentUserId(), dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    @PatchMapping("/gantt/{id}/reject")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> rejectTask(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        progressService.rejectTask(id, SecurityUtils.getCurrentUserId(), dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    @PatchMapping("/gantt/{id}/progress")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateTaskProgress(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BigDecimal pct = new BigDecimal(body.get("progressPct").toString());
        progressService.updateTaskProgress(id, pct, SecurityUtils.getCurrentUserId());
        return R.ok();
    }

    @GetMapping("/gantt/{id}/dependencies")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<BizTaskDependency>> getTaskDependencies(@PathVariable Integer id) {
        return R.ok(progressService.getTaskDependencies(id));
    }

    // ===================== 里程碑 /milestones =====================

    @GetMapping("/milestones")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<PageResult<MilestoneVO>> listMilestones(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(progressService.listMilestones(projectId, page, size));
    }

    @GetMapping("/milestones/all")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<MilestoneVO>> listAllMilestones(@RequestParam(required = false) Integer projectId) {
        return R.ok(progressService.listAllMilestones(projectId));
    }

    @PostMapping("/milestones")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> createMilestone(@Valid @RequestBody MilestoneDTO dto) {
        progressService.createMilestone(dto);
        return R.ok();
    }

    @PutMapping("/milestones/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateMilestone(@PathVariable Integer id, @Valid @RequestBody MilestoneDTO dto) {
        progressService.updateMilestone(id, dto);
        return R.ok();
    }

    @DeleteMapping("/milestones/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> deleteMilestone(@PathVariable Integer id) {
        progressService.deleteMilestone(id);
        return R.ok();
    }

    @GetMapping("/milestones/{id}/deps")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<Integer>> getMilestoneDeps(@PathVariable Integer id) {
        return R.ok(progressService.getMilestoneDeps(id));
    }

    // ===================== 里程碑状态流转 =====================

    @PatchMapping("/milestones/{id}/submit-approval")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> submitMilestoneApproval(@PathVariable Integer id) {
        progressService.submitMilestoneApproval(id, SecurityUtils.getCurrentUserId());
        return R.ok();
    }

    @PatchMapping("/milestones/{id}/approve")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> approveMilestone(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        progressService.approveMilestone(id, SecurityUtils.getCurrentUserId(), dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    @PatchMapping("/milestones/{id}/reject")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> rejectMilestone(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        progressService.rejectMilestone(id, SecurityUtils.getCurrentUserId(), dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    @PatchMapping("/milestones/{id}/lock")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> lockMilestone(@PathVariable Integer id) {
        progressService.lockMilestone(id, SecurityUtils.getCurrentUserId());
        return R.ok();
    }

    @PatchMapping("/milestones/{id}/complete")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> completeMilestone(@PathVariable Integer id, @RequestBody(required = false) ApprovalRemarkDTO dto) {
        progressService.completeMilestone(id, SecurityUtils.getCurrentUserId(), dto != null ? dto.getRemark() : null);
        return R.ok();
    }

    // ===================== 审计日志 =====================

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<PageResult<BizProgressAuditLog>> listAuditLogs(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer targetId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(auditService.listLogs(projectId, targetType, targetId, page, size));
    }

    // ===================== 预警管理 =====================

    @GetMapping("/warnings")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<WarningService.WarningItem>> scanWarnings(
            @RequestParam(required = false) Integer projectId) {
        return R.ok(warningService.scanWarnings(projectId));
    }

    @GetMapping("/warning-configs")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<List<BizWarningConfig>> listWarningConfigs(
            @RequestParam(required = false) Integer projectId) {
        return R.ok(warningService.listConfigs(projectId));
    }

    @PutMapping("/warning-configs/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateWarningConfig(@PathVariable Integer id, @RequestBody BizWarningConfig config) {
        warningService.updateConfig(id, config);
        return R.ok();
    }

    // ===================== 延期原因 =====================

    @PostMapping("/delay-reasons")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> addDelayReason(@RequestBody BizDelayReason reason) {
        reason.setCreatorId(SecurityUtils.getCurrentUserId());
        warningService.addDelayReason(reason);
        return R.ok();
    }

    @GetMapping("/gantt/{id}/delay-reasons")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<BizDelayReason>> listDelayReasons(@PathVariable Integer id) {
        return R.ok(warningService.listDelayReasons(id));
    }

    // ===================== 统计分析 =====================

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<ProgressStatService.ProjectStatVO> getProjectStats(
            @RequestParam(required = false) Integer projectId) {
        return R.ok(statService.getProjectStats(projectId));
    }

    @GetMapping("/stats/status-distribution")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<Map<String, Integer>> getStatusDistribution(
            @RequestParam(required = false) Integer projectId) {
        return R.ok(statService.getTaskStatusDistribution(projectId));
    }

    @PostMapping("/calc-critical-path")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> calcCriticalPath(@RequestParam(required = false) Integer projectId) {
        statService.calcCriticalPath(projectId);
        return R.ok();
    }

    // ===================== 导出 =====================

    @GetMapping("/export/progress-plan")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<String> exportProgressPlan(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String projectName) {
        return R.ok(exportService.generateProgressPlanHtml(projectId, projectName));
    }

    @GetMapping("/export/milestone-report")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<String> exportMilestoneReport(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String projectName) {
        return R.ok(exportService.generateMilestoneReportHtml(projectId, projectName));
    }

    @GetMapping("/export/acceptance/{milestoneId}")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<String> exportAcceptanceDoc(@PathVariable Integer milestoneId) {
        return R.ok(exportService.generateAcceptanceDocHtml(milestoneId));
    }

    // ===================== 变更单 /changes =====================

    @GetMapping("/changes")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<PageResult<BizChangeOrder>> listChangeOrders(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(progressService.listChangeOrders(projectId, changeType, status, page, size));
    }

    @GetMapping("/changes/{id}")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<BizChangeOrder> getChangeOrder(@PathVariable Integer id) {
        BizChangeOrder order = progressService.getChangeOrderById(id);
        if (order == null) return R.fail(404, "变更单不存在");
        return R.ok(order);
    }

    @GetMapping("/changes/{id}/details")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<BizChangeDetail>> listChangeDetails(@PathVariable Integer id) {
        return R.ok(progressService.listChangeDetails(id));
    }

    @PostMapping("/changes")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> createChangeOrder(@Valid @RequestBody ChangeOrderDTO dto) {
        progressService.createChangeOrder(dto);
        return R.ok();
    }

    @PutMapping("/changes/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateChangeOrder(@PathVariable Integer id, @Valid @RequestBody ChangeOrderDTO dto) {
        progressService.updateChangeOrder(id, dto);
        return R.ok();
    }

    @PatchMapping("/changes/{id}/status")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> updateChangeOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        progressService.updateChangeOrderStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/changes/{id}")
    @PreAuthorize("hasAuthority('progress:edit')")
    public R<Void> deleteChangeOrder(@PathVariable Integer id) {
        progressService.deleteChangeOrder(id);
        return R.ok();
    }
}
