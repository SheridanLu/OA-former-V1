package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.CaseDTO;
import com.mochu.business.dto.CompletionFinishDTO;
import com.mochu.business.dto.ExceptionTaskDTO;
import com.mochu.business.dto.LaborSettlementDTO;
import com.mochu.business.entity.BizCase;
import com.mochu.business.entity.BizCompletionFinish;
import com.mochu.business.entity.BizExceptionTask;
import com.mochu.business.entity.BizLaborSettlement;
import com.mochu.business.mapper.BizCaseMapper;
import com.mochu.business.mapper.BizCompletionFinishMapper;
import com.mochu.business.mapper.BizExceptionTaskMapper;
import com.mochu.business.mapper.BizLaborSettlementMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 竣工验收模块服务 — 完工验收 / 劳务结算 / 案例管理 / 异常工单
 */
@Service
@RequiredArgsConstructor
public class CompletionService {

    private final BizCompletionFinishMapper completionFinishMapper;
    private final BizLaborSettlementMapper laborSettlementMapper;
    private final BizCaseMapper caseMapper;
    private final BizExceptionTaskMapper exceptionTaskMapper;
    private final NoGeneratorService noGeneratorService;

    // ==================== 完工验收 ====================

    public PageResult<BizCompletionFinish> listFinish(Integer projectId, String status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizCompletionFinish> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizCompletionFinish> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizCompletionFinish::getProjectId, projectId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizCompletionFinish::getStatus, status);
        }
        wrapper.orderByDesc(BizCompletionFinish::getCreatedAt);

        completionFinishMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public BizCompletionFinish getFinishById(Integer id) {
        return completionFinishMapper.selectById(id);
    }

    public void createFinish(CompletionFinishDTO dto) {
        BizCompletionFinish entity = new BizCompletionFinish();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("draft");
        completionFinishMapper.insert(entity);
    }

    public void updateFinish(Integer id, CompletionFinishDTO dto) {
        BizCompletionFinish entity = completionFinishMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("完工验收记录不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        completionFinishMapper.updateById(entity);
    }

    public void updateFinishStatus(Integer id, String status) {
        BizCompletionFinish entity = completionFinishMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("完工验收记录不存在");
        }
        entity.setStatus(status);
        completionFinishMapper.updateById(entity);
    }

    public void deleteFinish(Integer id) {
        completionFinishMapper.deleteById(id);
    }

    // ==================== 劳务结算 ====================

    public PageResult<BizLaborSettlement> listLabor(Integer projectId, String status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizLaborSettlement> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizLaborSettlement> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizLaborSettlement::getProjectId, projectId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizLaborSettlement::getStatus, status);
        }
        wrapper.orderByDesc(BizLaborSettlement::getCreatedAt);

        laborSettlementMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public BizLaborSettlement getLaborById(Integer id) {
        return laborSettlementMapper.selectById(id);
    }

    public void createLabor(LaborSettlementDTO dto) {
        BizLaborSettlement entity = new BizLaborSettlement();
        BeanUtils.copyProperties(dto, entity);
        entity.setSettlementNo(noGeneratorService.generate("LS"));
        entity.setStatus("draft");
        laborSettlementMapper.insert(entity);
    }

    public void updateLabor(Integer id, LaborSettlementDTO dto) {
        BizLaborSettlement entity = laborSettlementMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("劳务结算记录不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        laborSettlementMapper.updateById(entity);
    }

    public void updateLaborStatus(Integer id, String status) {
        BizLaborSettlement entity = laborSettlementMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("劳务结算记录不存在");
        }
        entity.setStatus(status);
        laborSettlementMapper.updateById(entity);
    }

    public void deleteLabor(Integer id) {
        laborSettlementMapper.deleteById(id);
    }

    // ==================== 案例管理 ====================

    public PageResult<BizCase> listCase(Integer projectId, String status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizCase> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizCase> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizCase::getProjectId, projectId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizCase::getStatus, status);
        }
        wrapper.orderByAsc(BizCase::getDisplayOrder)
               .orderByDesc(BizCase::getCreatedAt);

        caseMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public BizCase getCaseById(Integer id) {
        return caseMapper.selectById(id);
    }

    public void createCase(CaseDTO dto) {
        BizCase entity = new BizCase();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("draft");
        caseMapper.insert(entity);
    }

    public void updateCase(Integer id, CaseDTO dto) {
        BizCase entity = caseMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("案例记录不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        caseMapper.updateById(entity);
    }

    public void updateCaseStatus(Integer id, String status) {
        BizCase entity = caseMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("案例记录不存在");
        }
        entity.setStatus(status);
        caseMapper.updateById(entity);
    }

    public void deleteCase(Integer id) {
        caseMapper.deleteById(id);
    }

    // ==================== 异常工单 ====================

    public PageResult<BizExceptionTask> listException(String bizType, Integer status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizExceptionTask> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizExceptionTask> wrapper = new LambdaQueryWrapper<>();
        if (bizType != null && !bizType.isBlank()) {
            wrapper.eq(BizExceptionTask::getBizType, bizType);
        }
        if (status != null) {
            wrapper.eq(BizExceptionTask::getStatus, status);
        }
        wrapper.orderByDesc(BizExceptionTask::getCreatedAt);

        exceptionTaskMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public BizExceptionTask getExceptionById(Integer id) {
        return exceptionTaskMapper.selectById(id);
    }

    public void createException(ExceptionTaskDTO dto) {
        BizExceptionTask entity = new BizExceptionTask();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus(1); // 1=待处理
        exceptionTaskMapper.insert(entity);
    }

    public void updateException(Integer id, ExceptionTaskDTO dto) {
        BizExceptionTask entity = exceptionTaskMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("异常工单不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        exceptionTaskMapper.updateById(entity);
    }

    public void resolveException(Integer id, String resolveRemark) {
        BizExceptionTask entity = exceptionTaskMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("异常工单不存在");
        }
        entity.setStatus(2); // 2=已处理
        entity.setResolveRemark(resolveRemark);
        exceptionTaskMapper.updateById(entity);
    }

    public void deleteException(Integer id) {
        exceptionTaskMapper.deleteById(id);
    }
}
