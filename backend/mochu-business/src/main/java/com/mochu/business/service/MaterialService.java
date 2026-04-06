package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.MaterialDTO;
import com.mochu.business.entity.BizMaterialBase;
import com.mochu.business.mapper.BizMaterialBaseMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final BizMaterialBaseMapper materialMapper;
    private final NoGeneratorService noGeneratorService;

    public PageResult<BizMaterialBase> list(String materialName, String category, String status, Integer page, Integer size) {
        if (page == null || page < 1) page = Constants.DEFAULT_PAGE;
        if (size == null || size < 1) size = Constants.DEFAULT_SIZE;

        Page<BizMaterialBase> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizMaterialBase> wrapper = new LambdaQueryWrapper<>();

        if (materialName != null && !materialName.isBlank()) {
            wrapper.like(BizMaterialBase::getMaterialName, materialName);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(BizMaterialBase::getCategory, category);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizMaterialBase::getStatus, status);
        }
        wrapper.orderByDesc(BizMaterialBase::getCreatedAt);

        materialMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizMaterialBase getById(Integer id) {
        return materialMapper.selectById(id);
    }

    public void create(MaterialDTO dto) {
        BizMaterialBase entity = new BizMaterialBase();
        BeanUtils.copyProperties(dto, entity);
        entity.setMaterialCode(noGeneratorService.generate("M", 6));
        if (entity.getStatus() == null) entity.setStatus("active");
        materialMapper.insert(entity);
    }

    public void update(Integer id, MaterialDTO dto) {
        BizMaterialBase entity = materialMapper.selectById(id);
        if (entity == null) throw new RuntimeException("材料不存在");
        BeanUtils.copyProperties(dto, entity, "id", "materialCode");
        materialMapper.updateById(entity);
    }

    public void delete(Integer id) {
        materialMapper.deleteById(id);
    }

    public List<BizMaterialBase> listAll() {
        return materialMapper.selectList(
                new LambdaQueryWrapper<BizMaterialBase>()
                        .select(BizMaterialBase::getId, BizMaterialBase::getMaterialCode, BizMaterialBase::getMaterialName, BizMaterialBase::getUnit, BizMaterialBase::getBasePrice)
                        .eq(BizMaterialBase::getStatus, "active")
                        .orderByAsc(BizMaterialBase::getMaterialCode));
    }
}
