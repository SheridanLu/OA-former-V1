package com.mochu.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GanttTaskDTO {

    private Integer id;

    @NotNull(message = "项目ID不能为空")
    private Integer projectId;

    private Integer parentId;

    /** 负责人ID */
    private Integer assigneeId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotNull(message = "任务类型不能为空")
    private Integer taskType;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private BigDecimal progressPct;

    /** 进度权重 */
    private BigDecimal weight;

    /** WBS编码 */
    private String wbsCode;

    private Integer sortOrder;

    /** 前置依赖任务列表 [{depTaskId, depType}] */
    private List<TaskDepItem> dependencies;

    @Data
    public static class TaskDepItem {
        private Integer depTaskId;
        private String depType;
    }
}
