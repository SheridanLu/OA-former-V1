package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 甘特图任务表 — WBS多级任务 + 里程碑
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gantt_task")
public class BizGanttTask extends BaseEntity {

    private Integer projectId;

    private Integer parentId;

    /** 负责人ID */
    private Integer assigneeId;

    private String taskName;

    /** 1里程碑/2任务 */
    private Integer taskType;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private BigDecimal progressPct;

    /** 进度权重(加权汇总) */
    private BigDecimal weight;

    /** 计划工期(天) */
    private Integer plannedDuration;

    /** 实际工期(天) */
    private Integer actualDuration;

    /** 延期天数 */
    private Integer delayDays;

    /** WBS编码(如1.2.3) */
    private String wbsCode;

    /** 层级(0=顶层) */
    private Integer level;

    /** 审批人ID */
    private Integer approverId;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 审批意见 */
    private String approveRemark;

    /** 关联合同ID */
    private Integer linkedContractId;

    /** 关联文档ID(逗号分隔) */
    private String linkedDocIds;

    /** 关联物料ID(逗号分隔) */
    private String linkedMaterialIds;

    /** 是否关键路径节点 */
    private Integer isCritical;

    /** 风险等级: low/medium/high */
    private String riskLevel;

    /** 自定义扩展字段(JSON) */
    private String customFields;

    /** 里程碑类型(自定义分类) */
    private String milestoneType;

    /** 依赖关系类型:FS/SS/FF/SF (旧字段,兼容) */
    private String dependencyType;

    private Integer dependencyTaskId;

    private Integer sortOrder;

    /** 任务:not_started/in_progress/pending_review/completed/rejected; 里程碑:draft/pending/approved/locked/completed */
    private String status;
}
