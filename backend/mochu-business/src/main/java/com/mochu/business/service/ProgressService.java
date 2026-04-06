package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.ChangeOrderDTO;
import com.mochu.business.dto.GanttTaskDTO;
import com.mochu.business.entity.BizChangeDetail;
import com.mochu.business.entity.BizChangeOrder;
import com.mochu.business.entity.BizGanttTask;
import com.mochu.business.mapper.BizChangeDetailMapper;
import com.mochu.business.mapper.BizChangeOrderMapper;
import com.mochu.business.mapper.BizGanttTaskMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 进度管理 & 变更管理服务
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final BizGanttTaskMapper ganttTaskMapper;
    private final BizChangeOrderMapper changeOrderMapper;
    private final BizChangeDetailMapper changeDetailMapper;
    private final NoGeneratorService noGeneratorService;

    // ===================== 甘特图任务 =====================

    /**
     * 按项目查询甘特图任务树（按 sortOrder 排序）
     */
    public List<BizGanttTask> listGanttTasks(Integer projectId) {
        return ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(BizGanttTask::getProjectId, projectId)
                        .orderByAsc(BizGanttTask::getSortOrder)
                        .orderByAsc(BizGanttTask::getId));
    }

    public BizGanttTask getGanttTaskById(Integer id) {
        return ganttTaskMapper.selectById(id);
    }

    public void createGanttTask(GanttTaskDTO dto) {
        BizGanttTask entity = new BizGanttTask();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getParentId() == null) {
            entity.setParentId(0);
        }
        entity.setStatus("draft");
        ganttTaskMapper.insert(entity);
    }

    public void updateGanttTask(Integer id, GanttTaskDTO dto) {
        BizGanttTask entity = ganttTaskMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("甘特图任务不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id");
        ganttTaskMapper.updateById(entity);
    }

    public void updateGanttTaskStatus(Integer id, String status) {
        BizGanttTask entity = ganttTaskMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("甘特图任务不存在");
        }
        entity.setStatus(status);
        ganttTaskMapper.updateById(entity);
    }

    public void deleteGanttTask(Integer id) {
        ganttTaskMapper.deleteById(id);
    }

    // ===================== 变更单 =====================

    /**
     * 分页查询变更单
     */
    public PageResult<BizChangeOrder> listChangeOrders(Integer projectId, String changeType,
                                                       String status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizChangeOrder> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizChangeOrder> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(BizChangeOrder::getProjectId, projectId);
        }
        if (changeType != null && !changeType.isBlank()) {
            wrapper.eq(BizChangeOrder::getChangeType, changeType);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(BizChangeOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizChangeOrder::getCreatedAt);

        changeOrderMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public BizChangeOrder getChangeOrderById(Integer id) {
        return changeOrderMapper.selectById(id);
    }

    /**
     * 查询变更单的明细列表
     */
    public List<BizChangeDetail> listChangeDetails(Integer changeId) {
        return changeDetailMapper.selectList(
                new LambdaQueryWrapper<BizChangeDetail>()
                        .eq(BizChangeDetail::getChangeId, changeId)
                        .orderByAsc(BizChangeDetail::getId));
    }

    @Transactional
    public void createChangeOrder(ChangeOrderDTO dto) {
        BizChangeOrder order = new BizChangeOrder();
        BeanUtils.copyProperties(dto, order, "details");
        order.setChangeNo(noGeneratorService.generate("CG"));
        order.setStatus("draft");
        changeOrderMapper.insert(order);

        // 保存明细
        if (dto.getDetails() != null) {
            for (ChangeOrderDTO.ChangeDetailItem item : dto.getDetails()) {
                BizChangeDetail detail = new BizChangeDetail();
                BeanUtils.copyProperties(item, detail);
                detail.setChangeId(order.getId());
                changeDetailMapper.insert(detail);
            }
        }
    }

    @Transactional
    public void updateChangeOrder(Integer id, ChangeOrderDTO dto) {
        BizChangeOrder order = changeOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("变更单不存在");
        }
        BeanUtils.copyProperties(dto, order, "id", "details");
        changeOrderMapper.updateById(order);

        // 先删除旧明细，再重新插入
        if (dto.getDetails() != null) {
            changeDetailMapper.delete(
                    new LambdaQueryWrapper<BizChangeDetail>()
                            .eq(BizChangeDetail::getChangeId, id));
            for (ChangeOrderDTO.ChangeDetailItem item : dto.getDetails()) {
                BizChangeDetail detail = new BizChangeDetail();
                BeanUtils.copyProperties(item, detail);
                detail.setId(null);
                detail.setChangeId(id);
                changeDetailMapper.insert(detail);
            }
        }
    }

    public void updateChangeOrderStatus(Integer id, String status) {
        BizChangeOrder order = changeOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("变更单不存在");
        }
        order.setStatus(status);
        changeOrderMapper.updateById(order);
    }

    public void deleteChangeOrder(Integer id) {
        changeOrderMapper.deleteById(id);
    }
}
