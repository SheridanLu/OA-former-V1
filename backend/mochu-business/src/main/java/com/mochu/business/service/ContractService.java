package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.ContractDTO;
import com.mochu.business.entity.BizContract;
import com.mochu.business.mapper.BizContractMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final BizContractMapper contractMapper;
    private final NoGeneratorService noGeneratorService;

    public PageResult<BizContract> list(String contractName, String contractType, String status,
                                         Integer projectId, Integer page, Integer size) {
        if (page == null || page < 1) page = Constants.DEFAULT_PAGE;
        if (size == null || size < 1) size = Constants.DEFAULT_SIZE;

        Page<BizContract> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizContract> wrapper = new LambdaQueryWrapper<>();

        if (contractName != null && !contractName.isBlank()) {
            wrapper.like(BizContract::getContractName, contractName);
        }
        if (contractType != null && !contractType.isBlank()) {
            wrapper.eq(BizContract::getContractType, contractType);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizContract::getStatus, status);
        }
        if (projectId != null) {
            wrapper.eq(BizContract::getProjectId, projectId);
        }
        wrapper.orderByDesc(BizContract::getCreatedAt);

        contractMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizContract getById(Integer id) {
        return contractMapper.selectById(id);
    }

    public void create(ContractDTO dto) {
        BizContract entity = new BizContract();
        BeanUtils.copyProperties(dto, entity);
        entity.setContractNo(noGeneratorService.generate("CT"));
        entity.setStatus("draft");
        if (dto.getTaxAmount() == null && dto.getAmountWithTax() != null && dto.getAmountWithoutTax() != null) {
            entity.setTaxAmount(dto.getAmountWithTax().subtract(dto.getAmountWithoutTax()));
        }
        contractMapper.insert(entity);
    }

    public void update(Integer id, ContractDTO dto) {
        BizContract entity = contractMapper.selectById(id);
        if (entity == null) throw new BusinessException("合同不存在");
        BeanUtils.copyProperties(dto, entity, "id");
        if (dto.getTaxAmount() == null && dto.getAmountWithTax() != null && dto.getAmountWithoutTax() != null) {
            entity.setTaxAmount(dto.getAmountWithTax().subtract(dto.getAmountWithoutTax()));
        }
        contractMapper.updateById(entity);
    }

    public void updateStatus(Integer id, String status) {
        BizContract entity = contractMapper.selectById(id);
        if (entity == null) throw new BusinessException("合同不存在");
        entity.setStatus(status);
        contractMapper.updateById(entity);
    }

    public void delete(Integer id) {
        contractMapper.deleteById(id);
    }
}
