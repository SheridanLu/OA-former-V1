package com.mochu.business.controller;

import com.mochu.business.dto.MaterialBatchDTO;
import com.mochu.business.dto.MaterialDTO;
import com.mochu.business.entity.BizMaterialBase;
import com.mochu.business.service.ApprovalService;
import com.mochu.business.service.MaterialService;
import com.mochu.business.vo.BatchResult;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final ApprovalService approvalService;

    @GetMapping
    @PreAuthorize("hasAuthority('material:view')")
    public R<PageResult<BizMaterialBase>> list(
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(materialService.list(materialName, category, status, page, size));
    }

    @GetMapping("/all")
    public R<List<BizMaterialBase>> listAll() {
        return R.ok(materialService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('material:view')")
    public R<BizMaterialBase> getById(@PathVariable Integer id) {
        BizMaterialBase material = materialService.getById(id);
        if (material == null) return R.fail(404, "材料不存在");
        return R.ok(material);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('material:edit')")
    public R<BatchResult> batchCreate(@Valid @RequestBody MaterialBatchDTO dto) {
        BatchResult result = materialService.batchCreate(dto);
        return R.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('material:edit')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody MaterialDTO dto) {
        materialService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('material:edit')")
    public R<Void> delete(@PathVariable Integer id) {
        materialService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("hasAuthority('material:edit')")
    public R<Void> submitApproval(@PathVariable Integer id) {
        BizMaterialBase material = materialService.getById(id);
        if (material == null) return R.fail(404, "材料不存在");
        Integer userId = SecurityUtils.getCurrentUserId();
        if (approvalService.hasFlowDef("material")) {
            approvalService.submitForApproval("material", id, userId);
        } else {
            return R.fail(400, "未配置材料审批流程");
        }
        return R.ok();
    }
}
