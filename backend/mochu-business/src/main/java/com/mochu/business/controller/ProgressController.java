package com.mochu.business.controller;

import com.mochu.business.dto.ChangeOrderDTO;
import com.mochu.business.dto.GanttTaskDTO;
import com.mochu.business.entity.BizChangeDetail;
import com.mochu.business.entity.BizChangeOrder;
import com.mochu.business.entity.BizGanttTask;
import com.mochu.business.service.ProgressService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 进度管理 & 变更管理接口
 */
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // ===================== 甘特图任务 /gantt =====================

    @GetMapping("/gantt")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<PageResult<BizGanttTask>> listGanttTasks(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(progressService.listGanttTasksPaged(projectId, page, size));
    }

    @GetMapping("/gantt/{id}")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<BizGanttTask> getGanttTask(@PathVariable Integer id) {
        BizGanttTask task = progressService.getGanttTaskById(id);
        if (task == null) {
            return R.fail(404, "甘特图任务不存在");
        }
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
        if (order == null) {
            return R.fail(404, "变更单不存在");
        }
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
