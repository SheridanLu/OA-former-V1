package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 零星采购表 — 对照 V3.2 P.29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_spot_purchase")
public class BizSpotPurchase extends BaseEntity {

    private String purchaseNo;

    private Integer projectId;

    private Integer supplierId;

    private Integer materialId;

    private String materialName;

    private String specModel;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private String status;

    private String remark;
}
