package com.mochu.business.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 甘特任务VO — 含依赖、子任务信息
 */
@Data
public class GanttTaskVO {

    private Integer id;
    private Integer projectId;
    private Integer parentId;
    private Integer assigneeId;
    private String assigneeName;
    private String taskName;
    private Integer taskType;

    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    private BigDecimal progressPct;
    private BigDecimal weight;
    private Integer plannedDuration;
    private Integer actualDuration;
    private Integer delayDays;

    private String wbsCode;
    private Integer level;
    private Integer sortOrder;
    private String status;

    private Integer approverId;
    private LocalDateTime approveTime;
    private String approveRemark;

    /** 关联合同ID */
    private Integer linkedContractId;
    /** 是否关键路径节点 */
    private Integer isCritical;
    /** 风险等级: low/medium/high */
    private String riskLevel;
    /** 里程碑类型 */
    private String milestoneType;

    private LocalDateTime createdAt;

    /** 前置依赖列表 */
    private List<DepItem> dependencies;

    /** 子任务列表(树形) */
    private List<GanttTaskVO> children;

    @Data
    public static class DepItem {
        private Integer depTaskId;
        private String depTaskName;
        private String depType;
        private String depStatus;
    }
}
