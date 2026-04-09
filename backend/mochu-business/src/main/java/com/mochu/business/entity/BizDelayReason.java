package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 延期原因记录
 */
@Data
@TableName("biz_delay_reason")
public class BizDelayReason {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 延期任务ID */
    private Integer taskId;

    /** 延期天数 */
    private Integer delayDays;

    /** 原因分类: material/labor/design/weather/other */
    private String reasonType;

    /** 延期原因详情 */
    private String reasonDetail;

    /** 关联整改任务ID */
    private Integer rectifyTaskId;

    private Integer creatorId;

    private LocalDateTime createdAt;
}
