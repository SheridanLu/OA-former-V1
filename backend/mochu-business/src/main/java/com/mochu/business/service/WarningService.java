package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.entity.BizDelayReason;
import com.mochu.business.entity.BizGanttTask;
import com.mochu.business.entity.BizWarningConfig;
import com.mochu.business.mapper.BizDelayReasonMapper;
import com.mochu.business.mapper.BizGanttTaskMapper;
import com.mochu.business.mapper.BizWarningConfigMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 预警与延期管理服务
 */
@Service
@RequiredArgsConstructor
public class WarningService {

    private final BizGanttTaskMapper ganttTaskMapper;
    private final BizWarningConfigMapper warningConfigMapper;
    private final BizDelayReasonMapper delayReasonMapper;

    // ===================== 预警扫描 =====================

    /**
     * 扫描项目预警消息
     */
    public List<WarningItem> scanWarnings(Integer projectId) {
        List<WarningItem> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 获取预警配置
        List<BizWarningConfig> configs = warningConfigMapper.selectList(
                new LambdaQueryWrapper<BizWarningConfig>()
                        .eq(BizWarningConfig::getEnabled, 1)
                        .and(w -> w.isNull(BizWarningConfig::getProjectId)
                                .or().eq(BizWarningConfig::getProjectId, projectId)));

        int dueSoonDays = 3, overdueDays = 0, milestoneRiskDays = 5;
        for (BizWarningConfig c : configs) {
            if ("due_soon".equals(c.getWarningType())) dueSoonDays = c.getThresholdDays();
            else if ("overdue".equals(c.getWarningType())) overdueDays = c.getThresholdDays();
            else if ("milestone_risk".equals(c.getWarningType())) milestoneRiskDays = c.getThresholdDays();
        }

        // 查询未完成的任务
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.notIn(BizGanttTask::getStatus, "completed", "locked");
        if (projectId != null) wrapper.eq(BizGanttTask::getProjectId, projectId);
        List<BizGanttTask> tasks = ganttTaskMapper.selectList(wrapper);

        for (BizGanttTask task : tasks) {
            if (task.getPlanEndDate() == null) continue;
            long daysRemaining = ChronoUnit.DAYS.between(today, task.getPlanEndDate());

            if (daysRemaining < 0) {
                // 已超期
                WarningItem w = new WarningItem();
                w.setType("overdue");
                w.setLevel("high");
                w.setTaskId(task.getId());
                w.setTaskName(task.getTaskName());
                w.setTaskType(task.getTaskType());
                w.setProjectId(task.getProjectId());
                w.setPlanEndDate(task.getPlanEndDate());
                w.setDelayDays((int) Math.abs(daysRemaining));
                w.setMessage(String.format("%s「%s」已超期%d天",
                        task.getTaskType() == 1 ? "里程碑" : "任务", task.getTaskName(), Math.abs(daysRemaining)));
                warnings.add(w);
            } else if (daysRemaining <= dueSoonDays) {
                // 即将到期
                WarningItem w = new WarningItem();
                w.setType("due_soon");
                w.setLevel(daysRemaining <= 1 ? "high" : "medium");
                w.setTaskId(task.getId());
                w.setTaskName(task.getTaskName());
                w.setTaskType(task.getTaskType());
                w.setProjectId(task.getProjectId());
                w.setPlanEndDate(task.getPlanEndDate());
                w.setDelayDays(0);
                w.setMessage(String.format("%s「%s」将在%d天后到期",
                        task.getTaskType() == 1 ? "里程碑" : "任务", task.getTaskName(), daysRemaining));
                warnings.add(w);
            }

            // 里程碑风险(进度低于预期)
            if (task.getTaskType() == 1 && task.getPlanEndDate() != null && task.getPlanStartDate() != null) {
                long totalDays = ChronoUnit.DAYS.between(task.getPlanStartDate(), task.getPlanEndDate());
                long elapsed = ChronoUnit.DAYS.between(task.getPlanStartDate(), today);
                if (totalDays > 0 && elapsed > 0) {
                    double expectedPct = Math.min(100.0, (double) elapsed / totalDays * 100);
                    double actualPct = task.getProgressPct() != null ? task.getProgressPct().doubleValue() : 0;
                    if (expectedPct - actualPct > milestoneRiskDays * 5) { // 每天5%偏差阈值
                        WarningItem w = new WarningItem();
                        w.setType("milestone_risk");
                        w.setLevel("medium");
                        w.setTaskId(task.getId());
                        w.setTaskName(task.getTaskName());
                        w.setTaskType(task.getTaskType());
                        w.setProjectId(task.getProjectId());
                        w.setPlanEndDate(task.getPlanEndDate());
                        w.setDelayDays(0);
                        w.setMessage(String.format("里程碑「%s」进度偏差过大(预期%.0f%%,实际%.0f%%)",
                                task.getTaskName(), expectedPct, actualPct));
                        warnings.add(w);
                    }
                }
            }
        }

        return warnings;
    }

    // ===================== 预警配置 =====================

    public List<BizWarningConfig> listConfigs(Integer projectId) {
        LambdaQueryWrapper<BizWarningConfig> w = new LambdaQueryWrapper<>();
        if (projectId != null) {
            w.and(q -> q.isNull(BizWarningConfig::getProjectId).or().eq(BizWarningConfig::getProjectId, projectId));
        }
        return warningConfigMapper.selectList(w);
    }

    public void updateConfig(Integer id, BizWarningConfig config) {
        BizWarningConfig existing = warningConfigMapper.selectById(id);
        if (existing == null) throw new BusinessException("配置不存在");
        existing.setThresholdDays(config.getThresholdDays());
        existing.setEnabled(config.getEnabled());
        existing.setNotifyRoles(config.getNotifyRoles());
        warningConfigMapper.updateById(existing);
    }

    // ===================== 延期原因管理 =====================

    /**
     * 检查延期任务是否已录入原因(强制)
     */
    public boolean hasDelayReason(Integer taskId) {
        return delayReasonMapper.selectCount(
                new LambdaQueryWrapper<BizDelayReason>().eq(BizDelayReason::getTaskId, taskId)) > 0;
    }

    /**
     * 录入延期原因
     */
    public void addDelayReason(BizDelayReason reason) {
        reason.setCreatedAt(LocalDateTime.now());
        delayReasonMapper.insert(reason);
    }

    /**
     * 查询任务的延期原因列表
     */
    public List<BizDelayReason> listDelayReasons(Integer taskId) {
        return delayReasonMapper.selectList(
                new LambdaQueryWrapper<BizDelayReason>()
                        .eq(BizDelayReason::getTaskId, taskId)
                        .orderByDesc(BizDelayReason::getCreatedAt));
    }

    /**
     * 分页查询延期原因(项目维度)
     */
    public PageResult<BizDelayReason> listDelayReasonsPaged(Integer projectId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        // 先查项目下延期的任务ID
        LambdaQueryWrapper<BizGanttTask> taskWrapper = new LambdaQueryWrapper<>();
        if (projectId != null) taskWrapper.eq(BizGanttTask::getProjectId, projectId);
        taskWrapper.gt(BizGanttTask::getDelayDays, 0).select(BizGanttTask::getId);
        List<BizGanttTask> delayedTasks = ganttTaskMapper.selectList(taskWrapper);

        if (delayedTasks.isEmpty()) return new PageResult<>(List.of(), 0L, p, s);

        List<Integer> taskIds = delayedTasks.stream().map(BizGanttTask::getId).toList();
        Page<BizDelayReason> pageParam = new Page<>(p, s);
        delayReasonMapper.selectPage(pageParam, new LambdaQueryWrapper<BizDelayReason>()
                .in(BizDelayReason::getTaskId, taskIds)
                .orderByDesc(BizDelayReason::getCreatedAt));
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    @Data
    public static class WarningItem {
        private String type;       // due_soon / overdue / milestone_risk
        private String level;      // low / medium / high
        private Integer taskId;
        private String taskName;
        private Integer taskType;
        private Integer projectId;
        private LocalDate planEndDate;
        private Integer delayDays;
        private String message;
    }
}
