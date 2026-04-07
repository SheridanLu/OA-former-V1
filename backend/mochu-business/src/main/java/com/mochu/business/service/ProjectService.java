package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.ProjectDTO;
import com.mochu.business.dto.ProjectQueryDTO;
import com.mochu.business.entity.BizProject;
import com.mochu.business.mapper.BizProjectMapper;
import com.mochu.business.vo.ProjectVO;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final BizProjectMapper projectMapper;
    private final NoGeneratorService noGeneratorService;
    private final ApprovalService approvalService;

    public PageResult<BizProject> list(ProjectQueryDTO query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? Constants.DEFAULT_PAGE : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? Constants.DEFAULT_SIZE : query.getSize();

        Page<BizProject> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<>();

        if (query.getProjectName() != null && !query.getProjectName().isBlank()) {
            wrapper.like(BizProject::getProjectName, query.getProjectName());
        }
        if (query.getProjectNo() != null && !query.getProjectNo().isBlank()) {
            wrapper.like(BizProject::getProjectNo, query.getProjectNo());
        }
        if (query.getProjectType() != null) {
            wrapper.eq(BizProject::getProjectType, query.getProjectType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(BizProject::getStatus, query.getStatus());
        }
        if (query.getManagerId() != null) {
            wrapper.eq(BizProject::getManagerId, query.getManagerId());
        }
        wrapper.orderByDesc(BizProject::getCreatedAt);

        projectMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), page, size);
    }

    public BizProject getById(Integer id) {
        return projectMapper.selectById(id);
    }

    @Transactional
    public void create(ProjectDTO dto, Integer initiatorId) {
        BizProject entity = new BizProject();
        BeanUtils.copyProperties(dto, entity);
        entity.setProjectNo(noGeneratorService.generate("PJ"));
        entity.setCreatorId(initiatorId);

        // 检查是否已配置审批流程
        boolean hasFlow = approvalService.hasFlowDef("project");
        entity.setStatus(hasFlow ? "pending" : "draft");
        projectMapper.insert(entity);

        if (hasFlow) {
            try {
                approvalService.submitForApproval("project", entity.getId(), initiatorId);
            } catch (Exception e) {
                log.warn("项目审批提交失败，保存为草稿: {}", e.getMessage());
                entity.setStatus("draft");
                projectMapper.updateById(entity);
            }
        }
    }

    public void update(Integer id, ProjectDTO dto) {
        BizProject entity = projectMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("项目不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        projectMapper.updateById(entity);
    }

    public void updateStatus(Integer id, String status) {
        BizProject entity = projectMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("项目不存在");
        }
        // 只有审批通过(active)的项目才允许手动变更状态
        if (!"active".equals(entity.getStatus()) && !"completed".equals(entity.getStatus())) {
            throw new BusinessException("项目尚未审批通过，无法变更状态");
        }
        entity.setStatus(status);
        projectMapper.updateById(entity);
    }

    public void delete(Integer id) {
        projectMapper.deleteById(id);
    }

    /**
     * 查询所有项目（下拉选择用）
     */
    public List<BizProject> listAll() {
        return projectMapper.selectList(
                new LambdaQueryWrapper<BizProject>()
                        .select(BizProject::getId, BizProject::getProjectNo, BizProject::getProjectName, BizProject::getStatus)
                        .orderByDesc(BizProject::getCreatedAt));
    }
}
