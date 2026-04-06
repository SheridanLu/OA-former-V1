package com.mochu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.system.entity.SysPermission;
import com.mochu.system.mapper.SysPermissionMapper;
import com.mochu.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 查询全部权限列表
     */
    public List<PermissionVO> listAll() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getModule, SysPermission::getId)
        );
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 按模块分组查询
     */
    public List<PermissionVO> listByModule(String module) {
        List<SysPermission> permissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getModule, module)
                        .orderByAsc(SysPermission::getId)
        );
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    private PermissionVO toVO(SysPermission perm) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(perm, vo);
        return vo;
    }
}
