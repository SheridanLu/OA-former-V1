package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.*;
import com.mochu.business.entity.*;
import com.mochu.business.mapper.*;
import com.mochu.common.constant.Constants;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 人力资源管理服务
 */
@Service
@RequiredArgsConstructor
public class HrService {

    private final BizSalaryMapper salaryMapper;
    private final BizHrContractMapper contractMapper;
    private final BizHrCertificateMapper certificateMapper;
    private final BizHrEntryMapper entryMapper;
    private final BizHrResignMapper resignMapper;
    private final NoGeneratorService noGeneratorService;

    // ======================= 薪资 =======================

    public PageResult<BizSalary> listSalaries(SalaryDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizSalary> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizSalary> wrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null) {
            wrapper.eq(BizSalary::getUserId, query.getUserId());
        }
        if (query.getSalaryMonth() != null && !query.getSalaryMonth().isBlank()) {
            wrapper.eq(BizSalary::getSalaryMonth, query.getSalaryMonth());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizSalary::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(BizSalary::getCreatedAt);

        salaryMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizSalary getSalaryById(Integer id) {
        return salaryMapper.selectById(id);
    }

    public void createSalary(SalaryDTO dto) {
        BizSalary entity = new BizSalary();
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        entity.setStatus("draft");
        salaryMapper.insert(entity);
    }

    public void updateSalary(Integer id, SalaryDTO dto) {
        BizSalary entity = salaryMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("薪资记录不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        salaryMapper.updateById(entity);
    }

    public void updateSalaryStatus(Integer id, String status) {
        BizSalary entity = salaryMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("薪资记录不存在");
        }
        entity.setStatus(status);
        salaryMapper.updateById(entity);
    }

    public void deleteSalary(Integer id) {
        salaryMapper.deleteById(id);
    }

    // ======================= 劳动合同 =======================

    public PageResult<BizHrContract> listContracts(HrContractDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizHrContract> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizHrContract> wrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null) {
            wrapper.eq(BizHrContract::getUserId, query.getUserId());
        }
        if (query.getContractType() != null && !query.getContractType().isBlank()) {
            wrapper.eq(BizHrContract::getContractType, query.getContractType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizHrContract::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(BizHrContract::getCreatedAt);

        contractMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizHrContract getContractById(Integer id) {
        return contractMapper.selectById(id);
    }

    public void createContract(HrContractDTO dto) {
        BizHrContract entity = new BizHrContract();
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        entity.setStatus("active");
        contractMapper.insert(entity);
    }

    public void updateContract(Integer id, HrContractDTO dto) {
        BizHrContract entity = contractMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("合同不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        contractMapper.updateById(entity);
    }

    public void updateContractStatus(Integer id, String status) {
        BizHrContract entity = contractMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("合同不存在");
        }
        entity.setStatus(status);
        contractMapper.updateById(entity);
    }

    public void deleteContract(Integer id) {
        contractMapper.deleteById(id);
    }

    // ======================= 证书管理 =======================

    public PageResult<BizHrCertificate> listCertificates(HrCertificateDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizHrCertificate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizHrCertificate> wrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null) {
            wrapper.eq(BizHrCertificate::getUserId, query.getUserId());
        }
        if (query.getCertType() != null && !query.getCertType().isBlank()) {
            wrapper.eq(BizHrCertificate::getCertType, query.getCertType());
        }
        if (query.getCertName() != null && !query.getCertName().isBlank()) {
            wrapper.like(BizHrCertificate::getCertName, query.getCertName());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizHrCertificate::getStatus, query.getStatus());
        }
        if (query.getWarnStatus() != null && !query.getWarnStatus().isBlank()) {
            wrapper.eq(BizHrCertificate::getWarnStatus, query.getWarnStatus());
        }
        wrapper.orderByDesc(BizHrCertificate::getCreatedAt);

        certificateMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizHrCertificate getCertificateById(Integer id) {
        return certificateMapper.selectById(id);
    }

    public void createCertificate(HrCertificateDTO dto) {
        BizHrCertificate entity = new BizHrCertificate();
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        entity.setStatus("active");
        entity.setWarnStatus("normal");
        certificateMapper.insert(entity);
    }

    public void updateCertificate(Integer id, HrCertificateDTO dto) {
        BizHrCertificate entity = certificateMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("证书不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        certificateMapper.updateById(entity);
    }

    public void updateCertificateStatus(Integer id, String status) {
        BizHrCertificate entity = certificateMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("证书不存在");
        }
        entity.setStatus(status);
        certificateMapper.updateById(entity);
    }

    public void deleteCertificate(Integer id) {
        certificateMapper.deleteById(id);
    }

    // ======================= 入职申请 =======================

    public PageResult<BizHrEntry> listEntries(HrEntryDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizHrEntry> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizHrEntry> wrapper = new LambdaQueryWrapper<>();

        if (query.getApplicantName() != null && !query.getApplicantName().isBlank()) {
            wrapper.like(BizHrEntry::getApplicantName, query.getApplicantName());
        }
        if (query.getDeptId() != null) {
            wrapper.eq(BizHrEntry::getDeptId, query.getDeptId());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizHrEntry::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(BizHrEntry::getCreatedAt);

        entryMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizHrEntry getEntryById(Integer id) {
        return entryMapper.selectById(id);
    }

    public void createEntry(HrEntryDTO dto) {
        BizHrEntry entity = new BizHrEntry();
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        entity.setEntryNo(noGeneratorService.generate("EN"));
        entity.setStatus("draft");
        entryMapper.insert(entity);
    }

    public void updateEntry(Integer id, HrEntryDTO dto) {
        BizHrEntry entity = entryMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("入职申请不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "page", "size", "entryNo");
        entryMapper.updateById(entity);
    }

    public void updateEntryStatus(Integer id, String status) {
        BizHrEntry entity = entryMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("入职申请不存在");
        }
        entity.setStatus(status);
        entryMapper.updateById(entity);
    }

    public void deleteEntry(Integer id) {
        entryMapper.deleteById(id);
    }

    // ======================= 离职申请 =======================

    public PageResult<BizHrResign> listResigns(HrResignDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizHrResign> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizHrResign> wrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null) {
            wrapper.eq(BizHrResign::getUserId, query.getUserId());
        }
        if (query.getResignType() != null && !query.getResignType().isBlank()) {
            wrapper.eq(BizHrResign::getResignType, query.getResignType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizHrResign::getStatus, query.getStatus());
        }
        if (query.getHandoverStatus() != null && !query.getHandoverStatus().isBlank()) {
            wrapper.eq(BizHrResign::getHandoverStatus, query.getHandoverStatus());
        }
        wrapper.orderByDesc(BizHrResign::getCreatedAt);

        resignMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizHrResign getResignById(Integer id) {
        return resignMapper.selectById(id);
    }

    public void createResign(HrResignDTO dto) {
        BizHrResign entity = new BizHrResign();
        BeanUtils.copyProperties(dto, entity, "id", "page", "size");
        entity.setResignNo(noGeneratorService.generate("RS"));
        entity.setStatus("draft");
        entity.setHandoverStatus("pending");
        resignMapper.insert(entity);
    }

    public void updateResign(Integer id, HrResignDTO dto) {
        BizHrResign entity = resignMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("离职申请不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "page", "size", "resignNo");
        resignMapper.updateById(entity);
    }

    public void updateResignStatus(Integer id, String status) {
        BizHrResign entity = resignMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("离职申请不存在");
        }
        entity.setStatus(status);
        resignMapper.updateById(entity);
    }

    public void deleteResign(Integer id) {
        resignMapper.deleteById(id);
    }
}
