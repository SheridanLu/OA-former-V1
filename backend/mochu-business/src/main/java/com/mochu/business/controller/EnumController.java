package com.mochu.business.controller;

import com.mochu.business.enums.ContractTypeEnum;
import com.mochu.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 枚举配置接口
 */
@RestController
@RequestMapping("/api/v1/enums")
public class EnumController {

    @GetMapping("/contract-types")
    public R<List<Map<String, String>>> contractTypes() {
        return R.ok(ContractTypeEnum.toList());
    }
}
