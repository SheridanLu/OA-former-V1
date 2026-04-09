package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.business.entity.BizGanttTask;
import com.mochu.business.mapper.BizGanttTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 进度报表导出服务 — 生成可打印的 HTML (进度计划/里程碑报告/验收文件)
 */
@Service
@RequiredArgsConstructor
public class ProgressExportService {

    private final BizGanttTaskMapper ganttTaskMapper;
    private final ProgressStatService statService;

    /**
     * 生成进度计划报表HTML(可直接PDF打印)
     */
    public String generateProgressPlanHtml(Integer projectId, String projectName) {
        List<BizGanttTask> tasks = ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(projectId != null, BizGanttTask::getProjectId, projectId)
                        .orderByAsc(BizGanttTask::getSortOrder).orderByAsc(BizGanttTask::getId));

        ProgressStatService.ProjectStatVO stat = statService.getProjectStats(projectId);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>进度计划报表</title>");
        sb.append("<style>");
        sb.append(printCss());
        sb.append("</style></head><body>");
        sb.append("<div class='watermark'></div>");
        sb.append("<div class='page-header'>").append(projectName != null ? projectName : "项目").append(" — 进度计划报表</div>");
        sb.append("<div class='meta'>报表日期: ").append(today)
          .append(" | 整体进度: ").append(stat.getOverallProgress()).append("%")
          .append(" | 任务总数: ").append(stat.getTaskTotal())
          .append(" | 已完成: ").append(stat.getTaskCompleted())
          .append(" | 超期: ").append(stat.getOverdueTaskCount())
          .append("</div>");

        // 任务列表
        sb.append("<table><thead><tr><th>WBS</th><th>任务名称</th><th>类型</th><th>计划开始</th><th>计划结束</th><th>计划工期</th><th>实际开始</th><th>实际结束</th><th>实际工期</th><th>延期</th><th>进度</th><th>状态</th></tr></thead><tbody>");

        for (BizGanttTask t : tasks) {
            String type = t.getTaskType() == 1 ? "里程碑" : "任务";
            String rowClass = t.getDelayDays() != null && t.getDelayDays() > 0 ? " class='delay'" : "";
            if (t.getIsCritical() != null && t.getIsCritical() == 1) rowClass = " class='critical'";

            sb.append("<tr").append(rowClass).append(">");
            sb.append("<td>").append(t.getWbsCode() != null ? t.getWbsCode() : "-").append("</td>");
            sb.append("<td>").append(t.getTaskName()).append("</td>");
            sb.append("<td>").append(type).append("</td>");
            sb.append("<td>").append(t.getPlanStartDate() != null ? t.getPlanStartDate() : "-").append("</td>");
            sb.append("<td>").append(t.getPlanEndDate() != null ? t.getPlanEndDate() : "-").append("</td>");
            sb.append("<td>").append(t.getPlannedDuration() != null ? t.getPlannedDuration() + "天" : "-").append("</td>");
            sb.append("<td>").append(t.getActualStartDate() != null ? t.getActualStartDate() : "-").append("</td>");
            sb.append("<td>").append(t.getActualEndDate() != null ? t.getActualEndDate() : "-").append("</td>");
            sb.append("<td>").append(t.getActualDuration() != null ? t.getActualDuration() + "天" : "-").append("</td>");
            sb.append("<td>").append(t.getDelayDays() != null && t.getDelayDays() > 0 ? t.getDelayDays() + "天" : "-").append("</td>");
            sb.append("<td>").append(t.getProgressPct() != null ? t.getProgressPct() + "%" : "0%").append("</td>");
            sb.append("<td>").append(statusLabel(t.getStatus())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("<div class='footer'>第 <span class='page-num'></span> 页 | 墨初OA系统 | ").append(today).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 生成里程碑报告HTML
     */
    public String generateMilestoneReportHtml(Integer projectId, String projectName) {
        List<BizGanttTask> milestones = ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(projectId != null, BizGanttTask::getProjectId, projectId)
                        .eq(BizGanttTask::getTaskType, 1)
                        .orderByAsc(BizGanttTask::getSortOrder).orderByAsc(BizGanttTask::getId));

        ProgressStatService.ProjectStatVO stat = statService.getProjectStats(projectId);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>里程碑报告</title>");
        sb.append("<style>").append(printCss()).append("</style></head><body>");
        sb.append("<div class='watermark'></div>");
        sb.append("<div class='page-header'>").append(projectName != null ? projectName : "项目").append(" — 里程碑报告</div>");
        sb.append("<div class='meta'>报告日期: ").append(today)
          .append(" | 里程碑总数: ").append(stat.getMilestoneTotal())
          .append(" | 已达成: ").append(stat.getMilestoneCompleted())
          .append(" | 达成率: ").append(stat.getMilestoneAchievementRate()).append("%")
          .append(" | 按期率: ").append(stat.getMilestoneTotal() > 0 && stat.getMilestoneCompleted() > 0 ?
                stat.getMilestoneOnTimeCount() * 100 / stat.getMilestoneCompleted() : 0).append("%")
          .append("</div>");

        sb.append("<table><thead><tr><th>序号</th><th>里程碑名称</th><th>类型</th><th>计划完成</th><th>实际完成</th><th>延期</th><th>进度</th><th>状态</th></tr></thead><tbody>");

        int idx = 1;
        for (BizGanttTask m : milestones) {
            String rowClass = m.getDelayDays() != null && m.getDelayDays() > 0 ? " class='delay'" : "";
            sb.append("<tr").append(rowClass).append(">");
            sb.append("<td>").append(idx++).append("</td>");
            sb.append("<td>").append(m.getTaskName()).append("</td>");
            sb.append("<td>").append(m.getMilestoneType() != null ? m.getMilestoneType() : "-").append("</td>");
            sb.append("<td>").append(m.getPlanEndDate() != null ? m.getPlanEndDate() : "-").append("</td>");
            sb.append("<td>").append(m.getActualEndDate() != null ? m.getActualEndDate() : "-").append("</td>");
            sb.append("<td>").append(m.getDelayDays() != null && m.getDelayDays() > 0 ? m.getDelayDays() + "天" : "-").append("</td>");
            sb.append("<td>").append(m.getProgressPct() != null ? m.getProgressPct() + "%" : "0%").append("</td>");
            sb.append("<td>").append(statusLabel(m.getStatus())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("<div class='footer'>第 <span class='page-num'></span> 页 | 墨初OA系统 | ").append(today).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 生成里程碑验收文件HTML
     */
    public String generateAcceptanceDocHtml(Integer milestoneId) {
        BizGanttTask ms = ganttTaskMapper.selectById(milestoneId);
        if (ms == null || ms.getTaskType() != 1) return "<p>里程碑不存在</p>";

        // 关联子任务
        List<BizGanttTask> children = ganttTaskMapper.selectList(
                new LambdaQueryWrapper<BizGanttTask>()
                        .eq(BizGanttTask::getParentId, milestoneId)
                        .eq(BizGanttTask::getTaskType, 2)
                        .orderByAsc(BizGanttTask::getSortOrder));

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>里程碑验收文件</title>");
        sb.append("<style>").append(printCss()).append("</style></head><body>");
        sb.append("<div class='watermark'></div>");
        sb.append("<div class='page-header'>里程碑验收确认书</div>");

        sb.append("<table class='info-table'>");
        sb.append("<tr><td class='label'>里程碑名称</td><td>").append(ms.getTaskName()).append("</td>");
        sb.append("<td class='label'>状态</td><td>").append(statusLabel(ms.getStatus())).append("</td></tr>");
        sb.append("<tr><td class='label'>计划完成日期</td><td>").append(ms.getPlanEndDate() != null ? ms.getPlanEndDate() : "-").append("</td>");
        sb.append("<td class='label'>实际完成日期</td><td>").append(ms.getActualEndDate() != null ? ms.getActualEndDate() : "-").append("</td></tr>");
        sb.append("<tr><td class='label'>完成进度</td><td>").append(ms.getProgressPct() != null ? ms.getProgressPct() + "%" : "0%").append("</td>");
        sb.append("<td class='label'>延期天数</td><td>").append(ms.getDelayDays() != null && ms.getDelayDays() > 0 ? ms.getDelayDays() + "天" : "无延期").append("</td></tr>");
        sb.append("</table>");

        if (!children.isEmpty()) {
            sb.append("<h3>关联任务完成情况</h3>");
            sb.append("<table><thead><tr><th>序号</th><th>任务名称</th><th>负责人</th><th>计划结束</th><th>实际结束</th><th>状态</th></tr></thead><tbody>");
            int idx = 1;
            for (BizGanttTask c : children) {
                sb.append("<tr><td>").append(idx++).append("</td>");
                sb.append("<td>").append(c.getTaskName()).append("</td>");
                sb.append("<td>").append(c.getAssigneeId() != null ? "ID:" + c.getAssigneeId() : "-").append("</td>");
                sb.append("<td>").append(c.getPlanEndDate() != null ? c.getPlanEndDate() : "-").append("</td>");
                sb.append("<td>").append(c.getActualEndDate() != null ? c.getActualEndDate() : "-").append("</td>");
                sb.append("<td>").append(statusLabel(c.getStatus())).append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("<div class='sign-area'>");
        sb.append("<div class='sign-block'><p>项目负责人签字:</p><div class='sign-line'></div><p>日期: ____________</p></div>");
        sb.append("<div class='sign-block'><p>验收确认人签字:</p><div class='sign-line'></div><p>日期: ____________</p></div>");
        sb.append("</div>");

        sb.append("<div class='footer'>墨初OA系统 | 里程碑验收文件 | ").append(today).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String statusLabel(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "not_started" -> "未开始";
            case "in_progress" -> "执行中";
            case "pending_review" -> "待审核";
            case "completed" -> "已完成";
            case "rejected" -> "已驳回";
            case "draft" -> "草稿";
            case "pending" -> "审批中";
            case "approved" -> "已审批";
            case "locked" -> "已锁定";
            default -> status;
        };
    }

    private String printCss() {
        return """
            @page { size: A4; margin: 20mm 15mm; }
            body { font-family: 'SimSun','Microsoft YaHei',sans-serif; font-size: 12px; color: #333; position: relative; }
            .watermark {
              position: fixed; top: 0; left: 0; width: 100%; height: 100%;
              background: repeating-linear-gradient(45deg, transparent, transparent 80px,
                rgba(0,0,0,0.02) 80px, rgba(0,0,0,0.02) 81px);
              background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Ctext x='50%25' y='50%25' font-size='18' fill='rgba(0,0,0,0.06)' text-anchor='middle' dominant-baseline='middle' transform='rotate(-30,100,100)' font-family='SimSun'%3E墨初%3C/text%3E%3C/svg%3E");
              pointer-events: none; z-index: 9999;
            }
            .page-header { text-align: center; font-size: 20px; font-weight: bold; margin-bottom: 16px; border-bottom: 2px solid #333; padding-bottom: 8px; }
            .meta { font-size: 11px; color: #666; margin-bottom: 16px; text-align: center; }
            table { width: 100%; border-collapse: collapse; margin-bottom: 16px; page-break-inside: auto; }
            th, td { border: 1px solid #ccc; padding: 5px 8px; text-align: center; font-size: 11px; }
            th { background: #f5f5f5; font-weight: bold; }
            tr.delay td { background: #fff2f0; color: #cf1322; }
            tr.critical td { background: #fffbe6; }
            .info-table { margin-bottom: 24px; }
            .info-table .label { background: #f5f5f5; font-weight: bold; width: 120px; text-align: right; }
            .sign-area { display: flex; justify-content: space-around; margin-top: 60px; }
            .sign-block { text-align: center; }
            .sign-line { width: 200px; border-bottom: 1px solid #333; height: 40px; margin: 8px auto; }
            .footer { position: fixed; bottom: 10mm; left: 0; width: 100%; text-align: center; font-size: 10px; color: #999; }
            h3 { font-size: 14px; margin: 20px 0 8px; border-left: 3px solid #409eff; padding-left: 8px; }
            @media print { .watermark { position: fixed; } }
            @counter-style page-counter { system: decimal; }
        """;
    }
}
