package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.entity.BizProgressAuditLog;
import com.mochu.business.mapper.BizProgressAuditLogMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 进度操作审计服务 — 全流程留痕
 */
@Service
@RequiredArgsConstructor
public class ProgressAuditService {

    private final BizProgressAuditLogMapper auditLogMapper;

    /**
     * 记录操作日志(异步)
     */
    @Async
    public void log(Integer projectId, String targetType, Integer targetId, String targetName,
                    String action, String fieldName, String oldValue, String newValue,
                    String remark, Integer operatorId) {
        BizProgressAuditLog log = new BizProgressAuditLog();
        log.setProjectId(projectId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setAction(action);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setOperatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    /**
     * 简化日志记录
     */
    public void log(Integer projectId, String targetType, Integer targetId, String targetName,
                    String action, Integer operatorId) {
        log(projectId, targetType, targetId, targetName, action, null, null, null, null, operatorId);
    }

    /**
     * 状态变更日志
     */
    public void logStatusChange(Integer projectId, String targetType, Integer targetId, String targetName,
                                String oldStatus, String newStatus, String remark, Integer operatorId) {
        log(projectId, targetType, targetId, targetName, "status_change", "status", oldStatus, newStatus, remark, operatorId);
    }

    /**
     * 进度变更日志
     */
    public void logProgressUpdate(Integer projectId, Integer targetId, String targetName,
                                  String oldPct, String newPct, Integer operatorId) {
        log(projectId, "task", targetId, targetName, "progress_update", "progressPct", oldPct, newPct, null, operatorId);
    }

    /**
     * 分页查询操作日志
     */
    public PageResult<BizProgressAuditLog> listLogs(Integer projectId, String targetType, Integer targetId,
                                                     Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizProgressAuditLog> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizProgressAuditLog> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) wrapper.eq(BizProgressAuditLog::getProjectId, projectId);
        if (targetType != null) wrapper.eq(BizProgressAuditLog::getTargetType, targetType);
        if (targetId != null) wrapper.eq(BizProgressAuditLog::getTargetId, targetId);
        wrapper.orderByDesc(BizProgressAuditLog::getOperatedAt);

        auditLogMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }
}
