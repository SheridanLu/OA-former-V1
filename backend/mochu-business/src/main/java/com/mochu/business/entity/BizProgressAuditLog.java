package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 进度操作审计日志
 */
@Data
@TableName("biz_progress_audit_log")
public class BizProgressAuditLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer projectId;

    /** 操作对象类型: task/milestone/change */
    private String targetType;

    private Integer targetId;

    /** 对象名称快照 */
    private String targetName;

    /** 操作: create/update/delete/status_change/progress_update/approve/reject/lock/complete */
    private String action;

    /** 变更字段名 */
    private String fieldName;

    /** 变更前值 */
    private String oldValue;

    /** 变更后值 */
    private String newValue;

    private String remark;

    private Integer operatorId;

    private LocalDateTime operatedAt;
}
