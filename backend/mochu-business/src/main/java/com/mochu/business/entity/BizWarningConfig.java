package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警配置
 */
@Data
@TableName("biz_warning_config")
public class BizWarningConfig {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 项目ID(NULL=全局) */
    private Integer projectId;

    /** 预警类型: due_soon/overdue/milestone_risk */
    private String warningType;

    /** 预警阈值(天) */
    private Integer thresholdDays;

    /** 是否启用 */
    private Integer enabled;

    /** 通知角色(逗号分隔) */
    private String notifyRoles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
