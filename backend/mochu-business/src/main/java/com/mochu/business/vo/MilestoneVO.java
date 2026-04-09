package com.mochu.business.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 里程碑 VO — 含依赖列表 + 关联任务统计
 */
@Data
public class MilestoneVO {

    private Integer id;
    private Integer projectId;
    private String taskName;
    private LocalDate planEndDate;
    private LocalDate actualEndDate;
    private BigDecimal progressPct;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;

    /** 审批人ID */
    private Integer approverId;
    private LocalDateTime approveTime;
    private String approveRemark;

    /** 前置依赖里程碑ID列表 */
    private List<Integer> depMilestoneIds;
    /** 前置依赖里程碑名称列表 */
    private List<String> depMilestoneNames;

    /** 关联子任务总数 */
    private Integer linkedTaskCount;
    /** 关联子任务已完成数 */
    private Integer completedTaskCount;
    /** 延期天数 */
    private Integer delayDays;
}
