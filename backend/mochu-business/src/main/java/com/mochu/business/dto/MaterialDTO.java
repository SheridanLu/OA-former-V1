package com.mochu.business.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialDTO {

    private Integer id;

    @NotBlank(message = "材料名称不能为空")
    private String materialName;

    private String specModel;

    @NotBlank(message = "计量单位不能为空")
    private String unit;

    private String category;

    private BigDecimal basePrice;

    private String status;
}
