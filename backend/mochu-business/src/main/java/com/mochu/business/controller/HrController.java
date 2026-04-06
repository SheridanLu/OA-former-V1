package com.mochu.business.controller;

import com.mochu.business.dto.*;
import com.mochu.business.entity.*;
import com.mochu.business.service.HrService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人力资源管理接口
 */
@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
public class HrController {

    private final HrService hrService;

    // ======================= 薪资 =======================

    @GetMapping("/salaries")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<BizSalary>> listSalaries(SalaryDTO query) {
        return R.ok(hrService.listSalaries(query));
    }

    @GetMapping("/salaries/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<BizSalary> getSalaryById(@PathVariable Integer id) {
        BizSalary salary = hrService.getSalaryById(id);
        if (salary == null) {
            return R.fail(404, "薪资记录不存在");
        }
        return R.ok(salary);
    }

    @PostMapping("/salaries")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> createSalary(@Valid @RequestBody SalaryDTO dto) {
        hrService.createSalary(dto);
        return R.ok();
    }

    @PutMapping("/salaries/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateSalary(@PathVariable Integer id, @Valid @RequestBody SalaryDTO dto) {
        hrService.updateSalary(id, dto);
        return R.ok();
    }

    @PatchMapping("/salaries/{id}/status")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateSalaryStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        hrService.updateSalaryStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/salaries/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> deleteSalary(@PathVariable Integer id) {
        hrService.deleteSalary(id);
        return R.ok();
    }

    // ======================= 劳动合同 =======================

    @GetMapping("/contracts")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<BizHrContract>> listContracts(HrContractDTO query) {
        return R.ok(hrService.listContracts(query));
    }

    @GetMapping("/contracts/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<BizHrContract> getContractById(@PathVariable Integer id) {
        BizHrContract contract = hrService.getContractById(id);
        if (contract == null) {
            return R.fail(404, "合同不存在");
        }
        return R.ok(contract);
    }

    @PostMapping("/contracts")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> createContract(@Valid @RequestBody HrContractDTO dto) {
        hrService.createContract(dto);
        return R.ok();
    }

    @PutMapping("/contracts/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateContract(@PathVariable Integer id, @Valid @RequestBody HrContractDTO dto) {
        hrService.updateContract(id, dto);
        return R.ok();
    }

    @PatchMapping("/contracts/{id}/status")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateContractStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        hrService.updateContractStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/contracts/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> deleteContract(@PathVariable Integer id) {
        hrService.deleteContract(id);
        return R.ok();
    }

    // ======================= 证书管理 =======================

    @GetMapping("/certificates")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<BizHrCertificate>> listCertificates(HrCertificateDTO query) {
        return R.ok(hrService.listCertificates(query));
    }

    @GetMapping("/certificates/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<BizHrCertificate> getCertificateById(@PathVariable Integer id) {
        BizHrCertificate certificate = hrService.getCertificateById(id);
        if (certificate == null) {
            return R.fail(404, "证书不存在");
        }
        return R.ok(certificate);
    }

    @PostMapping("/certificates")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> createCertificate(@Valid @RequestBody HrCertificateDTO dto) {
        hrService.createCertificate(dto);
        return R.ok();
    }

    @PutMapping("/certificates/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateCertificate(@PathVariable Integer id, @Valid @RequestBody HrCertificateDTO dto) {
        hrService.updateCertificate(id, dto);
        return R.ok();
    }

    @PatchMapping("/certificates/{id}/status")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateCertificateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        hrService.updateCertificateStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/certificates/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> deleteCertificate(@PathVariable Integer id) {
        hrService.deleteCertificate(id);
        return R.ok();
    }

    // ======================= 入职申请 =======================

    @GetMapping("/entries")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<BizHrEntry>> listEntries(HrEntryDTO query) {
        return R.ok(hrService.listEntries(query));
    }

    @GetMapping("/entries/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<BizHrEntry> getEntryById(@PathVariable Integer id) {
        BizHrEntry entry = hrService.getEntryById(id);
        if (entry == null) {
            return R.fail(404, "入职申请不存在");
        }
        return R.ok(entry);
    }

    @PostMapping("/entries")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> createEntry(@Valid @RequestBody HrEntryDTO dto) {
        hrService.createEntry(dto);
        return R.ok();
    }

    @PutMapping("/entries/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateEntry(@PathVariable Integer id, @Valid @RequestBody HrEntryDTO dto) {
        hrService.updateEntry(id, dto);
        return R.ok();
    }

    @PatchMapping("/entries/{id}/status")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateEntryStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        hrService.updateEntryStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/entries/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> deleteEntry(@PathVariable Integer id) {
        hrService.deleteEntry(id);
        return R.ok();
    }

    // ======================= 离职申请 =======================

    @GetMapping("/resigns")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<BizHrResign>> listResigns(HrResignDTO query) {
        return R.ok(hrService.listResigns(query));
    }

    @GetMapping("/resigns/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<BizHrResign> getResignById(@PathVariable Integer id) {
        BizHrResign resign = hrService.getResignById(id);
        if (resign == null) {
            return R.fail(404, "离职申请不存在");
        }
        return R.ok(resign);
    }

    @PostMapping("/resigns")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> createResign(@Valid @RequestBody HrResignDTO dto) {
        hrService.createResign(dto);
        return R.ok();
    }

    @PutMapping("/resigns/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateResign(@PathVariable Integer id, @Valid @RequestBody HrResignDTO dto) {
        hrService.updateResign(id, dto);
        return R.ok();
    }

    @PatchMapping("/resigns/{id}/status")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> updateResignStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        hrService.updateResignStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/resigns/{id}")
    @PreAuthorize("hasAuthority('hr:edit')")
    public R<Void> deleteResign(@PathVariable Integer id) {
        hrService.deleteResign(id);
        return R.ok();
    }
}
