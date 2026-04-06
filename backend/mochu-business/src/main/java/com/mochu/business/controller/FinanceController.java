package com.mochu.business.controller;

import com.mochu.business.dto.InvoiceDTO;
import com.mochu.business.dto.PaymentApplyDTO;
import com.mochu.business.dto.ReimburseDTO;
import com.mochu.business.dto.StatementDTO;
import com.mochu.business.entity.*;
import com.mochu.business.service.FinanceService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财务管理接口
 */
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    // ====================== 对账单 /statements ======================

    @GetMapping("/statements")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<BizStatement>> listStatements(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer contractId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(financeService.listStatements(projectId, contractId, status, page, size));
    }

    @GetMapping("/statements/{id}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<BizStatement> getStatement(@PathVariable Integer id) {
        BizStatement entity = financeService.getStatementById(id);
        if (entity == null) {
            return R.fail(404, "对账单不存在");
        }
        return R.ok(entity);
    }

    @PostMapping("/statements")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> createStatement(@Valid @RequestBody StatementDTO dto) {
        financeService.createStatement(dto);
        return R.ok();
    }

    @PutMapping("/statements/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateStatement(@PathVariable Integer id, @Valid @RequestBody StatementDTO dto) {
        financeService.updateStatement(id, dto);
        return R.ok();
    }

    @PatchMapping("/statements/{id}/status")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateStatementStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        financeService.updateStatementStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/statements/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> deleteStatement(@PathVariable Integer id) {
        financeService.deleteStatement(id);
        return R.ok();
    }

    // ====================== 付款申请 /payments ======================

    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<BizPaymentApply>> listPayments(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer contractId,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(financeService.listPayments(projectId, contractId, paymentType, status, page, size));
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<BizPaymentApply> getPayment(@PathVariable Integer id) {
        BizPaymentApply entity = financeService.getPaymentById(id);
        if (entity == null) {
            return R.fail(404, "付款申请不存在");
        }
        return R.ok(entity);
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> createPayment(@Valid @RequestBody PaymentApplyDTO dto) {
        financeService.createPayment(dto);
        return R.ok();
    }

    @PutMapping("/payments/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updatePayment(@PathVariable Integer id, @Valid @RequestBody PaymentApplyDTO dto) {
        financeService.updatePayment(id, dto);
        return R.ok();
    }

    @PatchMapping("/payments/{id}/status")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updatePaymentStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        financeService.updatePaymentStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/payments/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> deletePayment(@PathVariable Integer id) {
        financeService.deletePayment(id);
        return R.ok();
    }

    // ====================== 发票 /invoices ======================

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<BizInvoice>> listInvoices(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Integer bizId,
            @RequestParam(required = false) String invoiceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(financeService.listInvoices(bizType, bizId, invoiceType, status, page, size));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<BizInvoice> getInvoice(@PathVariable Integer id) {
        BizInvoice entity = financeService.getInvoiceById(id);
        if (entity == null) {
            return R.fail(404, "发票不存在");
        }
        return R.ok(entity);
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> createInvoice(@Valid @RequestBody InvoiceDTO dto) {
        financeService.createInvoice(dto);
        return R.ok();
    }

    @PutMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateInvoice(@PathVariable Integer id, @Valid @RequestBody InvoiceDTO dto) {
        financeService.updateInvoice(id, dto);
        return R.ok();
    }

    @PatchMapping("/invoices/{id}/status")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateInvoiceStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        financeService.updateInvoiceStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> deleteInvoice(@PathVariable Integer id) {
        financeService.deleteInvoice(id);
        return R.ok();
    }

    // ====================== 报销 /reimburses ======================

    @GetMapping("/reimburses")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<BizReimburse>> listReimburses(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String reimburseType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(financeService.listReimburses(deptId, projectId, reimburseType, status, page, size));
    }

    @GetMapping("/reimburses/{id}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<BizReimburse> getReimburse(@PathVariable Integer id) {
        BizReimburse entity = financeService.getReimburseById(id);
        if (entity == null) {
            return R.fail(404, "报销单不存在");
        }
        return R.ok(entity);
    }

    @PostMapping("/reimburses")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> createReimburse(@Valid @RequestBody ReimburseDTO dto) {
        financeService.createReimburse(dto);
        return R.ok();
    }

    @PutMapping("/reimburses/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateReimburse(@PathVariable Integer id, @Valid @RequestBody ReimburseDTO dto) {
        financeService.updateReimburse(id, dto);
        return R.ok();
    }

    @PatchMapping("/reimburses/{id}/status")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> updateReimburseStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        financeService.updateReimburseStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/reimburses/{id}")
    @PreAuthorize("hasAuthority('finance:edit')")
    public R<Void> deleteReimburse(@PathVariable Integer id) {
        financeService.deleteReimburse(id);
        return R.ok();
    }

    // ====================== 成本台账 /cost-ledger ======================

    @GetMapping("/cost-ledger")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<BizCostLedger>> listCostLedger(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String costType,
            @RequestParam(required = false) String costSubtype,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(financeService.listCostLedger(projectId, costType, costSubtype, page, size));
    }

    @GetMapping("/cost-ledger/{id}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<BizCostLedger> getCostLedger(@PathVariable Integer id) {
        BizCostLedger entity = financeService.getCostLedgerById(id);
        if (entity == null) {
            return R.fail(404, "成本台账记录不存在");
        }
        return R.ok(entity);
    }

    @GetMapping("/cost-ledger/project/{projectId}")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<List<BizCostLedger>> listCostLedgerByProject(@PathVariable Integer projectId) {
        return R.ok(financeService.listCostLedgerByProject(projectId));
    }
}
