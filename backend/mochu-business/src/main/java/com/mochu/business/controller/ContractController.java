package com.mochu.business.controller;

import com.mochu.business.dto.ContractDTO;
import com.mochu.business.entity.BizContract;
import com.mochu.business.service.ContractService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
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
    public R<BizContract> getById(@PathVariable Integer id) {
        BizContract contract = contractService.getById(id);
        if (contract == null) return R.fail(404, "合同不存在");
        return R.ok(contract);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> create(@Valid @RequestBody ContractDTO dto) {
        contractService.create(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:edit')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody ContractDTO dto) {
        contractService.update(id, dto);
        return R.ok();
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
