package com.mochu.business.controller;

import com.mochu.business.dto.ContractDTO;
import com.mochu.business.entity.BizContract;
import com.mochu.business.service.ContractService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasAuthority('contract:view')")
    public R<PageResult<BizContract>> list(
            @RequestParam(required = false) String contractName,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(contractService.list(contractName, contractType, status, projectId, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<Map<String, Object>> getById(@PathVariable Integer id) {
        return R.ok(contractService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> create(@Valid @RequestBody ContractDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        contractService.create(dto, userId);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody ContractDTO dto) {
        contractService.update(id, dto);
        return R.ok();
    }

    /**
     * 保存合同正文编辑 — PUT /api/v1/contracts/{id}/content
     */
    @PutMapping("/{id}/content")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> saveContent(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        contractService.saveContent(id, body.get("content"));
        return R.ok();
    }

    /**
     * 提交审批 — PATCH /api/v1/contracts/{id}/submit-approval
     */
    @PatchMapping("/{id}/submit-approval")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> submitApproval(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        contractService.submitApproval(id, userId);
        return R.ok();
    }

    /**
     * 审批通过 — PATCH /api/v1/contracts/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> approve(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        contractService.approve(id, body != null ? body.get("remark") : null, userId);
        return R.ok();
    }

    /**
     * 审批驳回 — PATCH /api/v1/contracts/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> reject(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        contractService.reject(id, body != null ? body.get("remark") : null, userId);
        return R.ok();
    }

    /**
     * 预览合同正文 — GET /api/v1/contracts/{id}/preview
     */
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<String> previewContent(@PathVariable Integer id) {
        return R.ok(contractService.previewContent(id));
    }

    /**
     * 生成可打印合同 — GET /api/v1/contracts/{id}/printable
     */
    @GetMapping("/{id}/printable")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<String> printable(@PathVariable Integer id) {
        return R.ok(contractService.generatePrintableContract(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        contractService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> delete(@PathVariable Integer id) {
        contractService.delete(id);
        return R.ok();
    }
}
