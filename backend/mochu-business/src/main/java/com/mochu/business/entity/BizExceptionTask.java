package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 异常工单表 — 对照 V3.2 P.71
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exception_task")
public class BizExceptionTask extends BaseEntity {

    private String bizType;

    private Integer bizId;

    private String failReason;

    private Integer handlerId;

    private String resolveRemark;

    /** 1待处理 / 2已处理 */
    private Integer status;
}
