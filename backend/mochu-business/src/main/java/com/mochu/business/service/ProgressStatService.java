package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.business.entity.BizGanttTask;
import com.mochu.business.mapper.BizGanttTaskMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进度统计分析服务
 */
@Service
@RequiredArgsConstructor
public class ProgressStatService {

    private final BizGanttTaskMapper ganttTaskMapper;

    /**
     * 项目整体统计
     */
    public ProjectStatVO getProjectStats(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(BizGanttTask::getProjectId, projectId);
        List<BizGanttTask> allTasks = ganttTaskMapper.selectList(wrapper);

        ProjectStatVO stat = new ProjectStatVO();
        LocalDate today = LocalDate.now();

        // 分离里程碑和任务
        List<BizGanttTask> milestones = allTasks.stream().filter(t -> t.getTaskType() == 1).toList();
        List<BizGanttTask> tasks = allTasks.stream().filter(t -> t.getTaskType() == 2).toList();

        // 里程碑统计
        stat.setMilestoneTotal(milestones.size());
        stat.setMilestoneCompleted((int) milestones.stream().filter(m -> "completed".equals(m.getStatus())).count());
        long milestoneOnTime = milestones.stream()
                .filter(m -> "completed".equals(m.getStatus()) && m.getActualEndDate() != null && m.getPlanEndDate() != null)
                .filter(m -> !m.getActualEndDate().isAfter(m.getPlanEndDate())).count();
        stat.setMilestoneOnTimeCount((int) milestoneOnTime);
        stat.setMilestoneAchievementRate(milestones.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(stat.getMilestoneCompleted()).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(stat.getMilestoneTotal()), 1, RoundingMode.HALF_UP));

        // 任务统计
        stat.setTaskTotal(tasks.size());
        stat.setTaskCompleted((int) tasks.stream().filter(t -> "completed".equals(t.getStatus())).count());
        stat.setTaskInProgress((int) tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count());
        stat.setTaskNotStarted((int) tasks.stream().filter(t -> "not_started".equals(t.getStatus())).count());
        stat.setTaskPendingReview((int) tasks.stream().filter(t -> "pending_review".equals(t.getStatus())).count());
        stat.setTaskRejected((int) tasks.stream().filter(t -> "rejected".equals(t.getStatus())).count());

        // 按期率
        long taskOnTime = tasks.stream()
                .filter(t -> "completed".equals(t.getStatus()) && t.getActualEndDate() != null && t.getPlanEndDate() != null)
                .filter(t -> !t.getActualEndDate().isAfter(t.getPlanEndDate())).count();
        stat.setTaskOnTimeCount((int) taskOnTime);
        int completedTaskCount = stat.getTaskCompleted();
        stat.setTaskOnTimeRate(completedTaskCount == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(taskOnTime * 100).divide(BigDecimal.valueOf(completedTaskCount), 1, RoundingMode.HALF_UP));

        // 延期统计
        stat.setOverdueTaskCount((int) tasks.stream()
                .filter(t -> !"completed".equals(t.getStatus()) && t.getPlanEndDate() != null && t.getPlanEndDate().isBefore(today)).count());
        stat.setOverdueMilestoneCount((int) milestones.stream()
                .filter(m -> !"completed".equals(m.getStatus()) && m.getPlanEndDate() != null && m.getPlanEndDate().isBefore(today)).count());

        // 整体进度(加权平均)
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedProgress = BigDecimal.ZERO;
        for (BizGanttTask task : allTasks) {
            if (task.getParentId() != null && task.getParentId() > 0) continue; // 仅顶层参与计算
            BigDecimal w = task.getWeight() != null ? task.getWeight() : BigDecimal.ONE;
            BigDecimal pct = task.getProgressPct() != null ? task.getProgressPct() : BigDecimal.ZERO;
            totalWeight = totalWeight.add(w);
            weightedProgress = weightedProgress.add(w.multiply(pct));
        }
        stat.setOverallProgress(totalWeight.compareTo(BigDecimal.ZERO) > 0 ?
                weightedProgress.divide(totalWeight, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 关键路径节点数
        stat.setCriticalPathCount((int) allTasks.stream()
                .filter(t -> t.getIsCritical() != null && t.getIsCritical() == 1).count());

        return stat;
    }

    /**
     * 任务状态分布
     */
    public Map<String, Integer> getTaskStatusDistribution(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(BizGanttTask::getProjectId, projectId);
        wrapper.eq(BizGanttTask::getTaskType, 2);
        List<BizGanttTask> tasks = ganttTaskMapper.selectList(wrapper);

        Map<String, Integer> dist = new HashMap<>();
        for (BizGanttTask task : tasks) {
            dist.merge(task.getStatus() != null ? task.getStatus() : "not_started", 1, Integer::sum);
        }
        return dist;
    }

    /**
     * 计算关键路径并标记
     * 简化版: 最长路径上的节点标记为关键路径
     */
    public void calcCriticalPath(Integer projectId) {
        LambdaQueryWrapper<BizGanttTask> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(BizGanttTask::getProjectId, projectId);
        List<BizGanttTask> allTasks = ganttTaskMapper.selectList(wrapper);

        // 先全部重置
        for (BizGanttTask task : allTasks) {
            task.setIsCritical(0);
            task.setRiskLevel(null);
        }

        LocalDate today = LocalDate.now();
        for (BizGanttTask task : allTasks) {
            if (task.getPlanEndDate() == null) continue;

            // 计算风险等级
            if (!"completed".equals(task.getStatus()) && !"locked".equals(task.getStatus())) {
                long daysRemaining = ChronoUnit.DAYS.between(today, task.getPlanEndDate());
                if (daysRemaining < 0) {
                    task.setRiskLevel("high");
                    task.setIsCritical(1);
                } else if (daysRemaining <= 3) {
                    task.setRiskLevel("medium");
                    task.setIsCritical(1);
                } else {
                    task.setRiskLevel("low");
                }
            }

            // 计划工期最长的路径标记为关键
            if (task.getPlannedDuration() != null && task.getPlannedDuration() > 14) {
                task.setIsCritical(1);
            }

            ganttTaskMapper.updateById(task);
        }
    }

    @Data
    public static class ProjectStatVO {
        private Integer milestoneTotal;
        private Integer milestoneCompleted;
        private Integer milestoneOnTimeCount;
        private BigDecimal milestoneAchievementRate;

        private Integer taskTotal;
        private Integer taskCompleted;
        private Integer taskInProgress;
        private Integer taskNotStarted;
        private Integer taskPendingReview;
        private Integer taskRejected;
        private Integer taskOnTimeCount;
        private BigDecimal taskOnTimeRate;

        private Integer overdueTaskCount;
        private Integer overdueMilestoneCount;
        private BigDecimal overallProgress;
        private Integer criticalPathCount;
    }
}
