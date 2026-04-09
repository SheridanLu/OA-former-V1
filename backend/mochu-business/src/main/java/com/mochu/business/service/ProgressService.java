package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.dto.ChangeOrderDTO;
import com.mochu.business.dto.GanttTaskDTO;
import com.mochu.business.dto.MilestoneDTO;
import com.mochu.business.entity.*;
import com.mochu.business.mapper.*;
import com.mochu.business.vo.GanttTaskVO;
import com.mochu.business.vo.MilestoneVO;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.system.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 进度管理 & 变更管理服务 — 含WBS层级、依赖约束、状态流转、进度回滚
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final BizGanttTaskMapper ganttTaskMapper;
    private final BizMilestoneDepMapper milestoneDepMapper;
    private final BizTaskDependencyMapper taskDependencyMapper;
    private final BizChangeOrderMapper changeOrderMapper;
    private final BizChangeDetailMapper changeDetailMapper;
    private final NoGeneratorService noGeneratorService;
    private final ProgressAuditService auditService;
    private final TodoService todoService;

    // ===================== 甘特图任务 =====================

    public List<BizGanttTask> listGanttTasks(Integer projectId) {
        return ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(BizGanttTask::getProjectId, projectId)
                        .orderByAsc(BizGanttTask::getSortOrder)
                        .orderByAsc(BizGanttTask::getId));
    }

    public PageResult<BizGanttTask> listGanttTasksPaged(Integer projectId, Integer taskType, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizGanttTask> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(BizGanttTask::getProjectId, projectId);
        }
        if (taskType != null) {
            wrapper.eq(BizGanttTask::getTaskType, taskType);
        }
        wrapper.orderByAsc(BizGanttTask::getSortOrder)
               .orderByAsc(BizGanttTask::getId);

        ganttTaskMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    /**
     * WBS树形查询 — 返回带依赖和层级的VO树
     */
    public List<GanttTaskVO> listGanttTree(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizGanttTask::getProjectId, projectId);
        }
        wrapper.orderByAsc(BizGanttTask::getSortOrder).orderByAsc(BizGanttTask::getId);
        List<BizGanttTask> allTasks = ganttTaskMapper.selectList(wrapper);

        // 批量查询依赖关系
        List<Integer> taskIds = allTasks.stream().map(BizGanttTask::getId).collect(Collectors.toList());
        Map<Integer, List<BizTaskDependency>> depMap = getTaskDepMap(taskIds);

        // 名称映射
        Map<Integer, BizGanttTask> taskMap = allTasks.stream()
                .collect(Collectors.toMap(BizGanttTask::getId, t -> t));

        // 转VO
        List<GanttTaskVO> voList = allTasks.stream().map(task -> {
            GanttTaskVO vo = toGanttVO(task);
            List<BizTaskDependency> deps = depMap.getOrDefault(task.getId(), Collections.emptyList());
            vo.setDependencies(deps.stream().map(d -> {
                GanttTaskVO.DepItem item = new GanttTaskVO.DepItem();
                item.setDepTaskId(d.getDepTaskId());
                item.setDepType(d.getDepType());
                BizGanttTask depTask = taskMap.get(d.getDepTaskId());
                if (depTask != null) {
                    item.setDepTaskName(depTask.getTaskName());
                    item.setDepStatus(depTask.getStatus());
                }
                return item;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        // 构建树形
        return buildTree(voList);
    }

    private List<GanttTaskVO> buildTree(List<GanttTaskVO> allVos) {
        Map<Integer, GanttTaskVO> voMap = new LinkedHashMap<>();
        allVos.forEach(vo -> voMap.put(vo.getId(), vo));

        List<GanttTaskVO> roots = new ArrayList<>();
        for (GanttTaskVO vo : allVos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                GanttTaskVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    private GanttTaskVO toGanttVO(BizGanttTask task) {
        GanttTaskVO vo = new GanttTaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }

    public BizGanttTask getGanttTaskById(Integer id) {
        return ganttTaskMapper.selectById(id);
    }

    @Transactional
    public void createGanttTask(GanttTaskDTO dto) {
        BizGanttTask entity = new BizGanttTask();
        BeanUtils.copyProperties(dto, entity, "dependencies");
        if (entity.getParentId() == null) {
            entity.setParentId(0);
        }
        entity.setStatus(entity.getTaskType() == 1 ? "draft" : "not_started");
        if (entity.getWeight() == null) entity.setWeight(BigDecimal.ONE);

        // 自动计算层级和WBS编码
        calcLevelAndWbs(entity);
        // 自动计算计划工期
        calcPlannedDuration(entity);

        ganttTaskMapper.insert(entity);

        // 保存依赖关系
        saveTaskDependencies(entity.getId(), dto.getDependencies());

        // 审计日志
        auditService.log(entity.getProjectId(), entity.getTaskType() == 1 ? "milestone" : "task",
                entity.getId(), entity.getTaskName(), "create", entity.getCreatorId());
    }

    @Transactional
    public void updateGanttTask(Integer id, GanttTaskDTO dto) {
        BizGanttTask entity = ganttTaskMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("任务不存在");
        }

        // 里程碑已锁定不可编辑
        if (entity.getTaskType() == 1 && "locked".equals(entity.getStatus())) {
            throw new BusinessException("里程碑已锁定，不可编辑");
        }

        BeanUtils.copyProperties(dto, entity, "id", "dependencies", "status");
        calcLevelAndWbs(entity);
        calcPlannedDuration(entity);
        calcActualDuration(entity);
        calcDelayDays(entity);
        ganttTaskMapper.updateById(entity);

        // 更新依赖关系
        if (dto.getDependencies() != null) {
            taskDependencyMapper.delete(new LambdaQueryWrapper<BizTaskDependency>()
                    .eq(BizTaskDependency::getTaskId, id));
            saveTaskDependencies(id, dto.getDependencies());
        }
    }

    @Transactional
    public void deleteGanttTask(Integer id) {
        ganttTaskMapper.deleteById(id);
        // 清理相关依赖
        taskDependencyMapper.delete(new LambdaQueryWrapper<BizTaskDependency>()
                .eq(BizTaskDependency::getTaskId, id)
                .or().eq(BizTaskDependency::getDepTaskId, id));
    }

    // ===================== 任务状态流转 =====================

    /**
     * 开始任务: not_started → in_progress
     * 检查前置依赖是否已完成(FS类型)
     */
    @Transactional
    public void startTask(Integer id, Integer userId) {
        BizGanttTask task = ganttTaskMapper.selectById(id);
        if (task == null || task.getTaskType() != 2) {
            throw new BusinessException("任务不存在");
        }
        if (!"not_started".equals(task.getStatus()) && !"rejected".equals(task.getStatus())) {
            throw new BusinessException("仅未开始或被驳回的任务可开始");
        }

        // 依赖检查
        checkDependenciesMet(id);

        task.setStatus("in_progress");
        task.setActualStartDate(LocalDate.now());
        ganttTaskMapper.updateById(task);

        auditService.logStatusChange(task.getProjectId(), "task", id, task.getTaskName(),
                "not_started", "in_progress", null, userId);
    }

    /**
     * 提交审核: in_progress → pending_review
     */
    public void submitForReview(Integer id, Integer userId) {
        BizGanttTask task = ganttTaskMapper.selectById(id);
        if (task == null || task.getTaskType() != 2) {
            throw new BusinessException("任务不存在");
        }
        if (!"in_progress".equals(task.getStatus())) {
            throw new BusinessException("仅执行中的任务可提交审核");
        }
        task.setStatus("pending_review");
        ganttTaskMapper.updateById(task);

        auditService.logStatusChange(task.getProjectId(), "task", id, task.getTaskName(),
                "in_progress", "pending_review", null, userId);

        // 创建待办 — 通知任务创建者审核
        if (task.getCreatorId() != null && !task.getCreatorId().equals(userId)) {
            todoService.createTodo(task.getCreatorId(), "task_review", id,
                    "任务待审核: " + task.getTaskName(), "任务\"" + task.getTaskName() + "\"已提交审核，请及时处理",
                    1, null, "/todos");
        }
    }

    /**
     * 审批通过: pending_review → completed
     */
    @Transactional
    public void approveTask(Integer id, Integer userId, String remark) {
        BizGanttTask task = ganttTaskMapper.selectById(id);
        if (task == null || task.getTaskType() != 2) {
            throw new BusinessException("任务不存在");
        }
        if (!"pending_review".equals(task.getStatus())) {
            throw new BusinessException("仅待审核的任务可审批");
        }
        task.setStatus("completed");
        task.setProgressPct(new BigDecimal(100));
        task.setActualEndDate(LocalDate.now());
        task.setApproverId(userId);
        task.setApproveTime(LocalDateTime.now());
        task.setApproveRemark(remark);
        calcActualDuration(task);
        calcDelayDays(task);
        ganttTaskMapper.updateById(task);

        // 自动回滚父任务进度
        rollupParentProgress(task.getParentId());

        auditService.logStatusChange(task.getProjectId(), "task", id, task.getTaskName(),
                "pending_review", "completed", remark, userId);

        // 自动关闭关联待办
        todoService.markDoneByBiz("task_review", id);
    }

    /**
     * 驳回: pending_review → rejected
     */
    @Transactional
    public void rejectTask(Integer id, Integer userId, String remark) {
        BizGanttTask task = ganttTaskMapper.selectById(id);
        if (task == null || task.getTaskType() != 2) {
            throw new BusinessException("任务不存在");
        }
        if (!"pending_review".equals(task.getStatus())) {
            throw new BusinessException("仅待审核的任务可驳回");
        }
        task.setStatus("rejected");
        task.setApproverId(userId);
        task.setApproveTime(LocalDateTime.now());
        task.setApproveRemark(remark);
        ganttTaskMapper.updateById(task);

        auditService.logStatusChange(task.getProjectId(), "task", id, task.getTaskName(),
                "pending_review", "rejected", remark, userId);

        // 关闭审核待办 + 通知负责人被驳回
        todoService.markDoneByBiz("task_review", id);
        if (task.getAssigneeId() != null) {
            todoService.createTodo(task.getAssigneeId(), "task_rejected", id,
                    "任务被驳回: " + task.getTaskName(),
                    "任务\"" + task.getTaskName() + "\"审核未通过" + (remark != null ? "，原因: " + remark : ""),
                    1, null, "/todos");
        }
    }

    /**
     * 手动更新任务进度(0-100)
     */
    @Transactional
    public void updateTaskProgress(Integer id, BigDecimal progressPct, Integer userId) {
        BizGanttTask task = ganttTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if ("completed".equals(task.getStatus()) || "locked".equals(task.getStatus())) {
            throw new BusinessException("已完成/已锁定的任务不可修改进度");
        }
        if (progressPct.compareTo(BigDecimal.ZERO) < 0 || progressPct.compareTo(new BigDecimal(100)) > 0) {
            throw new BusinessException("进度须在0-100之间");
        }
        String oldPct = task.getProgressPct() != null ? task.getProgressPct().toPlainString() : "0";
        task.setProgressPct(progressPct);
        ganttTaskMapper.updateById(task);

        // 自动回滚父任务/项目进度
        rollupParentProgress(task.getParentId());

        auditService.logProgressUpdate(task.getProjectId(), id, task.getTaskName(), oldPct, progressPct.toPlainString(), userId);
    }

    /**
     * 旧接口兼容
     */
    public void updateGanttTaskStatus(Integer id, String status) {
        BizGanttTask entity = ganttTaskMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("甘特图任务不存在");
        }
        entity.setStatus(status);
        ganttTaskMapper.updateById(entity);
    }

    // ===================== 依赖约束 =====================

    /**
     * 检查前置依赖是否满足(FS类型:前驱必须completed)
     */
    private void checkDependenciesMet(Integer taskId) {
        List<BizTaskDependency> deps = taskDependencyMapper.selectList(
                new LambdaQueryWrapper<BizTaskDependency>().eq(BizTaskDependency::getTaskId, taskId));
        if (deps.isEmpty()) return;

        for (BizTaskDependency dep : deps) {
            BizGanttTask depTask = ganttTaskMapper.selectById(dep.getDepTaskId());
            if (depTask == null) continue;

            String type = dep.getDepType() != null ? dep.getDepType() : "FS";
            switch (type) {
                case "FS": // Finish-to-Start: 前驱完成后才能开始
                    if (!"completed".equals(depTask.getStatus())) {
                        throw new BusinessException("前置任务\"" + depTask.getTaskName() + "\"尚未完成(FS约束)");
                    }
                    break;
                case "SS": // Start-to-Start: 前驱开始后才能开始
                    if ("not_started".equals(depTask.getStatus())) {
                        throw new BusinessException("前置任务\"" + depTask.getTaskName() + "\"尚未开始(SS约束)");
                    }
                    break;
                case "FF": // Finish-to-Finish: 前驱完成后才能完成(检查时机在审批)
                    break;
                case "SF": // Start-to-Finish: 前驱开始后才能完成
                    break;
            }
        }
    }

    /**
     * 获取任务的依赖列表
     */
    public List<BizTaskDependency> getTaskDependencies(Integer taskId) {
        return taskDependencyMapper.selectList(
                new LambdaQueryWrapper<BizTaskDependency>().eq(BizTaskDependency::getTaskId, taskId));
    }

    /**
     * 批量查询任务依赖关系
     */
    private Map<Integer, List<BizTaskDependency>> getTaskDepMap(List<Integer> taskIds) {
        if (taskIds.isEmpty()) return Map.of();
        List<BizTaskDependency> deps = taskDependencyMapper.selectList(
                new LambdaQueryWrapper<BizTaskDependency>().in(BizTaskDependency::getTaskId, taskIds));
        return deps.stream().collect(Collectors.groupingBy(BizTaskDependency::getTaskId));
    }

    private void saveTaskDependencies(Integer taskId, List<GanttTaskDTO.TaskDepItem> deps) {
        if (deps == null || deps.isEmpty()) return;
        for (GanttTaskDTO.TaskDepItem item : deps) {
            if (item.getDepTaskId() == null || item.getDepTaskId().equals(taskId)) continue;
            BizTaskDependency dep = new BizTaskDependency();
            dep.setTaskId(taskId);
            dep.setDepTaskId(item.getDepTaskId());
            dep.setDepType(item.getDepType() != null ? item.getDepType() : "FS");
            dep.setCreatedAt(LocalDateTime.now());
            taskDependencyMapper.insert(dep);
        }
    }

    // ===================== 进度回滚 =====================

    /**
     * 根据子任务加权计算父任务进度
     */
    @Transactional
    public void rollupParentProgress(Integer parentId) {
        if (parentId == null || parentId == 0) return;

        List<BizGanttTask> children = ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>().eq(BizGanttTask::getParentId, parentId));
        if (children.isEmpty()) return;

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedProgress = BigDecimal.ZERO;

        for (BizGanttTask child : children) {
            BigDecimal w = child.getWeight() != null ? child.getWeight() : BigDecimal.ONE;
            BigDecimal pct = child.getProgressPct() != null ? child.getProgressPct() : BigDecimal.ZERO;
            totalWeight = totalWeight.add(w);
            weightedProgress = weightedProgress.add(w.multiply(pct));
        }

        BigDecimal rollupPct = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            rollupPct = weightedProgress.divide(totalWeight, 2, RoundingMode.HALF_UP);
        }

        BizGanttTask parent = ganttTaskMapper.selectById(parentId);
        if (parent != null) {
            parent.setProgressPct(rollupPct);
            ganttTaskMapper.updateById(parent);
            // 递归向上
            rollupParentProgress(parent.getParentId());
        }
    }

    // ===================== 工期计算 =====================

    private void calcLevelAndWbs(BizGanttTask entity) {
        if (entity.getParentId() == null || entity.getParentId() == 0) {
            entity.setLevel(0);
            if (entity.getWbsCode() == null || entity.getWbsCode().isBlank()) {
                // 自动生成顶层WBS
                long count = ganttTaskMapper.selectCount(
                        new LambdaQueryWrapper<BizGanttTask>()
                                .eq(BizGanttTask::getProjectId, entity.getProjectId())
                                .and(w -> w.isNull(BizGanttTask::getParentId).or().eq(BizGanttTask::getParentId, 0)));
                entity.setWbsCode(String.valueOf(count + 1));
            }
        } else {
            BizGanttTask parent = ganttTaskMapper.selectById(entity.getParentId());
            if (parent != null) {
                entity.setLevel(parent.getLevel() != null ? parent.getLevel() + 1 : 1);
                if (entity.getWbsCode() == null || entity.getWbsCode().isBlank()) {
                    long childCount = ganttTaskMapper.selectCount(
                            new LambdaQueryWrapper<BizGanttTask>()
                                    .eq(BizGanttTask::getParentId, entity.getParentId()));
                    String parentWbs = parent.getWbsCode() != null ? parent.getWbsCode() : "0";
                    entity.setWbsCode(parentWbs + "." + (childCount + 1));
                }
            }
        }
    }

    private void calcPlannedDuration(BizGanttTask entity) {
        if (entity.getPlanStartDate() != null && entity.getPlanEndDate() != null) {
            entity.setPlannedDuration((int) ChronoUnit.DAYS.between(entity.getPlanStartDate(), entity.getPlanEndDate()) + 1);
        }
    }

    private void calcActualDuration(BizGanttTask entity) {
        if (entity.getActualStartDate() != null && entity.getActualEndDate() != null) {
            entity.setActualDuration((int) ChronoUnit.DAYS.between(entity.getActualStartDate(), entity.getActualEndDate()) + 1);
        }
    }

    private void calcDelayDays(BizGanttTask entity) {
        if (entity.getPlanEndDate() != null && entity.getActualEndDate() != null) {
            long delay = ChronoUnit.DAYS.between(entity.getPlanEndDate(), entity.getActualEndDate());
            entity.setDelayDays(delay > 0 ? (int) delay : 0);
        } else if (entity.getPlanEndDate() != null && entity.getActualEndDate() == null
                && !"completed".equals(entity.getStatus()) && !"not_started".equals(entity.getStatus())) {
            long delay = ChronoUnit.DAYS.between(entity.getPlanEndDate(), LocalDate.now());
            entity.setDelayDays(delay > 0 ? (int) delay : 0);
        }
    }

    // ===================== 里程碑管理 =====================

    public PageResult<MilestoneVO> listMilestones(Integer projectId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizGanttTask> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGanttTask::getTaskType, 1);
        if (projectId != null) {
            wrapper.eq(BizGanttTask::getProjectId, projectId);
        }
        wrapper.orderByAsc(BizGanttTask::getSortOrder).orderByAsc(BizGanttTask::getId);
        ganttTaskMapper.selectPage(pageParam, wrapper);

        List<Integer> milestoneIds = pageParam.getRecords().stream()
                .map(BizGanttTask::getId).collect(Collectors.toList());
        Map<Integer, List<Integer>> depMap = getMilestoneDepMap(milestoneIds);
        Map<Integer, String> nameMap = getMilestoneNameMap(projectId);

        // 统计关联子任务
        Map<Integer, Integer> linkedCountMap = new HashMap<>();
        Map<Integer, Integer> completedCountMap = new HashMap<>();
        if (!milestoneIds.isEmpty()) {
            List<BizGanttTask> childTasks = ganttTaskMapper.selectList(
                    new LambdaQueryWrapper<BizGanttTask>()
                            .eq(BizGanttTask::getTaskType, 2)
                            .in(BizGanttTask::getParentId, milestoneIds));
            for (BizGanttTask child : childTasks) {
                linkedCountMap.merge(child.getParentId(), 1, Integer::sum);
                if ("completed".equals(child.getStatus())) {
                    completedCountMap.merge(child.getParentId(), 1, Integer::sum);
                }
            }
        }

        List<MilestoneVO> voList = pageParam.getRecords().stream().map(task -> {
            MilestoneVO vo = toMilestoneVO(task, depMap, nameMap);
            vo.setLinkedTaskCount(linkedCountMap.getOrDefault(task.getId(), 0));
            vo.setCompletedTaskCount(completedCountMap.getOrDefault(task.getId(), 0));
            if (task.getPlanEndDate() != null) {
                LocalDate checkDate = task.getActualEndDate() != null ? task.getActualEndDate() : LocalDate.now();
                long delay = ChronoUnit.DAYS.between(task.getPlanEndDate(), checkDate);
                vo.setDelayDays(delay > 0 ? (int) delay : 0);
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageParam.getTotal(), p, s);
    }

    public List<MilestoneVO> listAllMilestones(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGanttTask::getTaskType, 1);
        if (projectId != null) {
            wrapper.eq(BizGanttTask::getProjectId, projectId);
        }
        wrapper.orderByAsc(BizGanttTask::getSortOrder).orderByAsc(BizGanttTask::getId);
        List<BizGanttTask> tasks = ganttTaskMapper.selectList(wrapper);

        List<Integer> milestoneIds = tasks.stream().map(BizGanttTask::getId).collect(Collectors.toList());
        Map<Integer, List<Integer>> depMap = getMilestoneDepMap(milestoneIds);
        Map<Integer, String> nameMap = getMilestoneNameMap(projectId);

        return tasks.stream().map(task -> toMilestoneVO(task, depMap, nameMap)).collect(Collectors.toList());
    }

    private MilestoneVO toMilestoneVO(BizGanttTask task, Map<Integer, List<Integer>> depMap, Map<Integer, String> nameMap) {
        MilestoneVO vo = new MilestoneVO();
        vo.setId(task.getId());
        vo.setProjectId(task.getProjectId());
        vo.setTaskName(task.getTaskName());
        vo.setPlanEndDate(task.getPlanEndDate());
        vo.setActualEndDate(task.getActualEndDate());
        vo.setProgressPct(task.getProgressPct());
        vo.setSortOrder(task.getSortOrder());
        vo.setStatus(task.getStatus());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setApproverId(task.getApproverId());
        vo.setApproveTime(task.getApproveTime());
        vo.setApproveRemark(task.getApproveRemark());

        List<Integer> depIds = depMap.getOrDefault(task.getId(), new ArrayList<>());
        vo.setDepMilestoneIds(depIds);
        vo.setDepMilestoneNames(depIds.stream()
                .map(id -> nameMap.getOrDefault(id, "ID:" + id))
                .collect(Collectors.toList()));
        return vo;
    }

    @Transactional
    public void createMilestone(MilestoneDTO dto) {
        BizGanttTask entity = new BizGanttTask();
        entity.setProjectId(dto.getProjectId());
        entity.setTaskName(dto.getMilestoneName());
        entity.setTaskType(1);
        entity.setParentId(0);
        entity.setPlanEndDate(dto.getDeadline());
        entity.setActualEndDate(dto.getActualEndDate());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setStatus("draft");
        entity.setWeight(BigDecimal.ONE);
        ganttTaskMapper.insert(entity);

        saveMilestoneDeps(entity.getId(), dto.getDepMilestoneIds());
    }

    @Transactional
    public void updateMilestone(Integer id, MilestoneDTO dto) {
        BizGanttTask entity = ganttTaskMapper.selectById(id);
        if (entity == null || entity.getTaskType() != 1) {
            throw new BusinessException("里程碑不存在");
        }
        if ("locked".equals(entity.getStatus())) {
            throw new BusinessException("已锁定的里程碑不可编辑，需先提交变更审批");
        }
        entity.setProjectId(dto.getProjectId());
        entity.setTaskName(dto.getMilestoneName());
        entity.setPlanEndDate(dto.getDeadline());
        entity.setActualEndDate(dto.getActualEndDate());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        ganttTaskMapper.updateById(entity);

        milestoneDepMapper.delete(new LambdaQueryWrapper<BizMilestoneDep>()
                .eq(BizMilestoneDep::getMilestoneId, id));
        saveMilestoneDeps(id, dto.getDepMilestoneIds());
    }

    @Transactional
    public void deleteMilestone(Integer id) {
        ganttTaskMapper.deleteById(id);
        milestoneDepMapper.delete(new LambdaQueryWrapper<BizMilestoneDep>()
                .eq(BizMilestoneDep::getMilestoneId, id)
                .or().eq(BizMilestoneDep::getDepMilestoneId, id));
    }

    public List<Integer> getMilestoneDeps(Integer milestoneId) {
        return milestoneDepMapper.selectList(
                new LambdaQueryWrapper<BizMilestoneDep>()
                        .eq(BizMilestoneDep::getMilestoneId, milestoneId))
                .stream().map(BizMilestoneDep::getDepMilestoneId)
                .collect(Collectors.toList());
    }

    // ===================== 里程碑状态流转 =====================

    /**
     * 提交里程碑审批: draft → pending
     */
    public void submitMilestoneApproval(Integer id, Integer userId) {
        BizGanttTask ms = ganttTaskMapper.selectById(id);
        if (ms == null || ms.getTaskType() != 1) throw new BusinessException("里程碑不存在");
        if (!"draft".equals(ms.getStatus()) && !"rejected".equals(ms.getStatus())) {
            throw new BusinessException("仅草稿或被驳回的里程碑可提交审批");
        }
        ms.setStatus("pending");
        ganttTaskMapper.updateById(ms);

        auditService.logStatusChange(ms.getProjectId(), "milestone", id, ms.getTaskName(),
                "draft", "pending", null, userId);

        // 创建待办 — 通知里程碑创建者审批(如不是自己提交)
        if (ms.getCreatorId() != null && !ms.getCreatorId().equals(userId)) {
            todoService.createTodo(ms.getCreatorId(), "milestone_approval", id,
                    "里程碑待审批: " + ms.getTaskName(), "里程碑\"" + ms.getTaskName() + "\"已提交审批，请及时处理",
                    1, null, "/todos");
        }
    }

    /**
     * 审批通过: pending → approved
     */
    @Transactional
    public void approveMilestone(Integer id, Integer userId, String remark) {
        BizGanttTask ms = ganttTaskMapper.selectById(id);
        if (ms == null || ms.getTaskType() != 1) throw new BusinessException("里程碑不存在");
        if (!"pending".equals(ms.getStatus())) throw new BusinessException("仅审批中的里程碑可审批通过");

        ms.setStatus("approved");
        ms.setApproverId(userId);
        ms.setApproveTime(LocalDateTime.now());
        ms.setApproveRemark(remark);
        ganttTaskMapper.updateById(ms);

        auditService.logStatusChange(ms.getProjectId(), "milestone", id, ms.getTaskName(),
                "pending", "approved", remark, userId);

        // 关闭审批待办
        todoService.markDoneByBiz("milestone_approval", id);
    }

    /**
     * 驳回里程碑: pending → rejected(由draft用于里程碑)
     */
    public void rejectMilestone(Integer id, Integer userId, String remark) {
        BizGanttTask ms = ganttTaskMapper.selectById(id);
        if (ms == null || ms.getTaskType() != 1) throw new BusinessException("里程碑不存在");
        if (!"pending".equals(ms.getStatus())) throw new BusinessException("仅审批中的里程碑可驳回");

        ms.setStatus("draft");
        ms.setApproverId(userId);
        ms.setApproveTime(LocalDateTime.now());
        ms.setApproveRemark(remark);
        ganttTaskMapper.updateById(ms);

        auditService.logStatusChange(ms.getProjectId(), "milestone", id, ms.getTaskName(),
                "pending", "draft", remark, userId);

        // 关闭审批待办 + 通知提交人被驳回
        todoService.markDoneByBiz("milestone_approval", id);
        if (ms.getCreatorId() != null) {
            todoService.createTodo(ms.getCreatorId(), "milestone_rejected", id,
                    "里程碑被驳回: " + ms.getTaskName(),
                    "里程碑\"" + ms.getTaskName() + "\"审批未通过" + (remark != null ? "，原因: " + remark : ""),
                    1, null, "/todos");
        }
    }

    /**
     * 锁定里程碑: approved → locked (审批通过后自动锁定)
     */
    public void lockMilestone(Integer id, Integer userId) {
        BizGanttTask ms = ganttTaskMapper.selectById(id);
        if (ms == null || ms.getTaskType() != 1) throw new BusinessException("里程碑不存在");
        if (!"approved".equals(ms.getStatus())) throw new BusinessException("仅已审批的里程碑可锁定");

        ms.setStatus("locked");
        ganttTaskMapper.updateById(ms);

        auditService.logStatusChange(ms.getProjectId(), "milestone", id, ms.getTaskName(),
                "approved", "locked", null, userId);
    }

    /**
     * 完成里程碑: locked → completed
     * 自动检测关联子任务是否全部完成
     */
    @Transactional
    public void completeMilestone(Integer id, Integer userId, String remark) {
        BizGanttTask ms = ganttTaskMapper.selectById(id);
        if (ms == null || ms.getTaskType() != 1) throw new BusinessException("里程碑不存在");
        if (!"locked".equals(ms.getStatus()) && !"approved".equals(ms.getStatus())) {
            throw new BusinessException("仅已锁定/已审批的里程碑可标记完成");
        }

        // 检查关联子任务是否全部完成
        List<BizGanttTask> children = ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(BizGanttTask::getParentId, id)
                        .eq(BizGanttTask::getTaskType, 2));
        if (!children.isEmpty()) {
            long incompleteCount = children.stream()
                    .filter(c -> !"completed".equals(c.getStatus()))
                    .count();
            if (incompleteCount > 0) {
                throw new BusinessException("尚有" + incompleteCount + "个关联子任务未完成，无法完成里程碑");
            }
        }

        ms.setStatus("completed");
        ms.setProgressPct(new BigDecimal(100));
        ms.setActualEndDate(LocalDate.now());
        ms.setApproverId(userId);
        ms.setApproveTime(LocalDateTime.now());
        ms.setApproveRemark(remark);
        ganttTaskMapper.updateById(ms);

        auditService.logStatusChange(ms.getProjectId(), "milestone", id, ms.getTaskName(),
                "locked", "completed", remark, userId);
    }

    // ---- 内部方法 ----

    private void saveMilestoneDeps(Integer milestoneId, List<Integer> depIds) {
        if (depIds == null || depIds.isEmpty()) return;
        for (Integer depId : depIds) {
            if (depId.equals(milestoneId)) continue;
            BizMilestoneDep dep = new BizMilestoneDep();
            dep.setMilestoneId(milestoneId);
            dep.setDepMilestoneId(depId);
            dep.setCreatedAt(LocalDateTime.now());
            milestoneDepMapper.insert(dep);
        }
    }

    private Map<Integer, List<Integer>> getMilestoneDepMap(List<Integer> milestoneIds) {
        if (milestoneIds.isEmpty()) return Map.of();
        List<BizMilestoneDep> deps = milestoneDepMapper.selectList(
                new LambdaQueryWrapper<BizMilestoneDep>()
                        .in(BizMilestoneDep::getMilestoneId, milestoneIds));
        return deps.stream().collect(Collectors.groupingBy(
                BizMilestoneDep::getMilestoneId,
                Collectors.mapping(BizMilestoneDep::getDepMilestoneId, Collectors.toList())));
    }

    private Map<Integer, String> getMilestoneNameMap(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> w = new LambdaQueryWrapper<>();
        w.eq(BizGanttTask::getTaskType, 1);
        if (projectId != null) w.eq(BizGanttTask::getProjectId, projectId);
        w.select(BizGanttTask::getId, BizGanttTask::getTaskName);
        return ganttTaskMapper.selectList(w).stream()
                .collect(Collectors.toMap(BizGanttTask::getId, BizGanttTask::getTaskName));
    }

    // ===================== 变更单 =====================

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
            throw new BusinessException("变更单不存在");
        }
        BeanUtils.copyProperties(dto, order, "id", "details");
        changeOrderMapper.updateById(order);

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
            throw new BusinessException("变更单不存在");
        }
        String oldStatus = order.getStatus();
        order.setStatus(status);
        changeOrderMapper.updateById(order);

        // 变更单提交审批 → 创建待办; 审批通过/驳回 → 关闭待办
        if ("pending".equals(status) && !"pending".equals(oldStatus)) {
            if (order.getCreatorId() != null) {
                todoService.createTodo(order.getCreatorId(), "change_approval", id,
                        "变更单待审批: " + order.getTitle(),
                        "变更单\"" + order.getTitle() + "\"已提交审批",
                        1, null, "/todos");
            }
        } else if ("approved".equals(status) || "rejected".equals(status)) {
            todoService.markDoneByBiz("change_approval", id);
        }
    }

    public void deleteChangeOrder(Integer id) {
        changeOrderMapper.deleteById(id);
    }
}
