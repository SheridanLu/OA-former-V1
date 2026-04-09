<template>
  <div class="progress-page">
    <!-- 顶部项目筛选 + 预警摘要 -->
    <el-card shadow="never">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <el-form inline style="margin-bottom: 0">
          <el-form-item label="关联项目">
            <el-select v-model="filterProjectId" filterable clearable placeholder="全部项目" style="width: 220px" @change="handleProjectChange">
              <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-badge :value="warningCount" :hidden="warningCount === 0" type="danger">
            <el-button type="warning" plain size="small" @click="showWarningPanel = true">预警中心</el-button>
          </el-badge>
          <el-button plain size="small" @click="showAuditLog = true">操作日志</el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ stats.overallProgress || 0 }}%</div><div class="stat-lbl">整体进度</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ stats.milestoneCompleted || 0 }}/{{ stats.milestoneTotal || 0 }}</div><div class="stat-lbl">里程碑达成</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ stats.milestoneAchievementRate || 0 }}%</div><div class="stat-lbl">达成率</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ stats.taskOnTimeRate || 0 }}%</div><div class="stat-lbl">按期率</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card warn"><div class="stat-val">{{ stats.overdueTaskCount || 0 }}</div><div class="stat-lbl">超期任务</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ stats.criticalPathCount || 0 }}</div><div class="stat-lbl">关键路径</div></el-card></el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 12px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 里程碑管理 ==================== -->
        <el-tab-pane label="里程碑管理" name="milestone">
          <div style="margin-bottom: 12px; display: flex; gap: 8px">
            <el-button type="primary" @click="handleAddMilestone">新建里程碑</el-button>
            <el-button plain size="small" @click="handleExportMilestoneReport">导出报告</el-button>
          </div>
          <!-- 里程碑时间线 -->
          <div v-if="milestoneData.length > 0" class="milestone-timeline-wrapper">
            <div class="milestone-timeline">
              <svg :width="timelineSvgWidth" :height="timelineSvgHeight" class="milestone-dep-lines">
                <defs><marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#409eff" /></marker></defs>
                <line v-for="(line, idx) in depLines" :key="idx" :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2" stroke="#409eff" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#arrowhead)" />
              </svg>
              <div v-for="(ms, idx) in milestoneData" :key="ms.id" class="milestone-node" :style="{ left: milestoneNodePos(idx).x + 'px', top: milestoneNodePos(idx).y + 'px' }">
                <div class="milestone-diamond" :class="['ms-' + (ms.status || 'draft'), ms.delay_days > 0 ? 'ms-overdue' : '']" :title="ms.task_name">
                  <span class="milestone-icon">&#9670;</span>
                </div>
                <div class="milestone-label">{{ ms.task_name }}</div>
                <div class="milestone-date">{{ ms.plan_end_date || '未设置' }}</div>
                <el-tag :type="msStatusTagType[ms.status]" size="small" style="margin-top: 2px">{{ msStatusLabel[ms.status] || ms.status }}</el-tag>
              </div>
            </div>
          </div>
          <!-- 里程碑列表 -->
          <el-table :data="milestoneData" v-loading="loading" stripe border style="margin-top: 12px">
            <el-table-column prop="task_name" label="里程碑名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="项目" width="140"><template #default="{ row }">{{ projectNameMap[row.project_id] || row.project_id }}</template></el-table-column>
            <el-table-column prop="plan_end_date" label="计划完成" width="110" />
            <el-table-column prop="actual_end_date" label="实际完成" width="110" />
            <el-table-column label="进度" width="90"><template #default="{ row }"><el-progress :percentage="Number(row.progress_pct || 0)" :stroke-width="14" :text-inside="true" /></template></el-table-column>
            <el-table-column label="任务" width="80"><template #default="{ row }">{{ row.completed_task_count || 0 }}/{{ row.linked_task_count || 0 }}</template></el-table-column>
            <el-table-column label="延期" width="70"><template #default="{ row }"><span :class="{ 'text-danger': row.delay_days > 0 }">{{ row.delay_days || 0 }}天</span></template></el-table-column>
            <el-table-column label="依赖" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.dep_milestone_names && row.dep_milestone_names.length"><el-tag v-for="name in row.dep_milestone_names" :key="name" size="small" type="info" style="margin: 0 2px 2px 0">{{ name }}</el-tag></template>
                <span v-else style="color: #999">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="msStatusTagType[row.status]" size="small">{{ msStatusLabel[row.status] || row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <el-button v-if="!['locked','completed'].includes(row.status)" type="primary" link size="small" @click="handleEditMilestone(row)">编辑</el-button>
                <el-button v-if="row.status === 'draft'" type="warning" link size="small" @click="handleMsSubmitApproval(row)">提交审批</el-button>
                <el-button v-if="row.status === 'pending'" type="success" link size="small" @click="handleMsApprove(row)">通过</el-button>
                <el-button v-if="row.status === 'pending'" type="danger" link size="small" @click="handleMsReject(row)">驳回</el-button>
                <el-button v-if="row.status === 'approved'" link size="small" @click="handleMsLock(row)">锁定</el-button>
                <el-button v-if="['locked','approved'].includes(row.status)" type="success" link size="small" @click="handleMsComplete(row)">完成</el-button>
                <el-button v-if="['locked','completed'].includes(row.status)" type="info" link size="small" @click="handleExportAcceptance(row)">验收文件</el-button>
                <el-button v-if="!['locked','completed'].includes(row.status)" type="danger" link size="small" @click="handleDeleteMilestone(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="milestoneTotal > 0" style="margin-top: 16px; justify-content: flex-end" background layout="total, prev, pager, next" :total="milestoneTotal" v-model:current-page="milestonePage" @current-change="fetchMilestones" />
        </el-tab-pane>

        <!-- ==================== WBS任务管理 ==================== -->
        <el-tab-pane label="WBS任务管理" name="gantt">
          <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center">
            <el-button type="primary" @click="handleAddGantt">新建任务</el-button>
            <el-button plain size="small" @click="handleCalcCriticalPath">计算关键路径</el-button>
            <el-button plain size="small" @click="handleExportProgressPlan">导出进度计划</el-button>
            <el-radio-group v-model="viewMode" size="small" style="margin-left: 12px">
              <el-radio-button value="gantt">甘特图</el-radio-button>
              <el-radio-button value="list">列表</el-radio-button>
              <el-radio-button value="calendar">日历</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 甘特图视图 -->
          <div v-if="viewMode === 'gantt' && flatGanttData.length > 0" class="gantt-chart-wrapper">
            <table class="gantt-chart" border="0" cellspacing="0">
              <thead><tr>
                <th style="min-width: 240px; text-align: left; padding: 6px 8px">任务名称</th>
                <th style="width: 60px">权重</th>
                <th style="width: 80px">进度</th>
                <th style="width: 80px">延期</th>
                <th style="width: 80px">状态</th>
                <th style="min-width: 400px; text-align: left; padding: 6px 8px">时间线</th>
              </tr></thead>
              <tbody>
                <tr v-for="row in flatGanttData" :key="row.id" :class="ganttRowClass(row)">
                  <td style="padding: 6px 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                    <span :style="{ paddingLeft: (row._level || 0) * 16 + 'px' }">
                      <span v-if="row.task_type === 1" style="font-weight: bold">&#9670; {{ row.task_name }}</span>
                      <span v-else>{{ row.wbs_code ? row.wbs_code + ' ' : '' }}{{ row.task_name }}</span>
                    </span>
                  </td>
                  <td style="text-align: center">{{ row.weight || 1 }}</td>
                  <td style="text-align: center">{{ row.progress_pct || 0 }}%</td>
                  <td style="text-align: center"><span :class="{ 'text-danger': row.delay_days > 0 }">{{ row.delay_days || 0 }}</span></td>
                  <td style="text-align: center"><el-tag :type="taskStatusTagType[row.status]" size="small">{{ taskStatusLabel[row.status] || row.status }}</el-tag></td>
                  <td style="padding: 6px 8px">
                    <div class="gantt-bar-container">
                      <div class="gantt-bar" :style="ganttBarStyle(row)" :class="{ 'gantt-bar-critical': row.is_critical, 'gantt-bar-overdue': row.delay_days > 0 }" :title="`${row.plan_start_date || '?'} ~ ${row.plan_end_date || '?'}`">
                        <div class="gantt-bar-fill" :style="{ width: (row.progress_pct || 0) + '%' }"></div>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 列表视图 -->
          <el-table v-if="viewMode === 'list'" :data="ganttTreeData" v-loading="loading" stripe border row-key="id" :tree-props="{ children: 'children' }" default-expand-all style="margin-top: 12px">
            <el-table-column label="任务名称" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.is_critical" style="color: #e6a23c; margin-right: 4px" title="关键路径">&#9733;</span>
                <span v-if="row.task_type === 1" style="font-weight: bold">&#9670; {{ row.task_name }}</span>
                <span v-else>{{ row.wbs_code ? row.wbs_code + ' ' : '' }}{{ row.task_name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="负责人" width="80"><template #default="{ row }">{{ row.assignee_name || row.assignee_id || '-' }}</template></el-table-column>
            <el-table-column prop="weight" label="权重" width="60" align="center" />
            <el-table-column prop="plan_start_date" label="计划开始" width="100" />
            <el-table-column prop="plan_end_date" label="计划结束" width="100" />
            <el-table-column label="工期" width="60" align="center"><template #default="{ row }">{{ row.planned_duration || '-' }}</template></el-table-column>
            <el-table-column label="延期" width="60" align="center"><template #default="{ row }"><span :class="{ 'text-danger': row.delay_days > 0 }">{{ row.delay_days || 0 }}</span></template></el-table-column>
            <el-table-column label="风险" width="60" align="center"><template #default="{ row }"><el-tag v-if="row.risk_level" :type="riskTagType[row.risk_level]" size="small">{{ riskLabel[row.risk_level] }}</el-tag></template></el-table-column>
            <el-table-column label="进度" width="110"><template #default="{ row }"><el-progress :percentage="Number(row.progress_pct || 0)" :stroke-width="14" :text-inside="true" /></template></el-table-column>
            <el-table-column label="依赖" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.dependencies && row.dependencies.length"><el-tag v-for="d in row.dependencies" :key="d.dep_task_id" size="small" type="info" style="margin: 0 2px 2px 0">{{ d.dep_type }} {{ d.dep_task_name || '#' + d.dep_task_id }}</el-tag></template>
                <span v-else style="color: #999">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="taskStatusTagType[row.status]" size="small">{{ taskStatusLabel[row.status] || row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <template v-if="row.task_type === 2">
                  <el-button v-if="['not_started','rejected'].includes(row.status)" type="success" link size="small" @click="handleStartTask(row)">开始</el-button>
                  <el-button v-if="row.status === 'in_progress'" type="warning" link size="small" @click="handleSubmitReview(row)">提交审核</el-button>
                  <el-button v-if="row.status === 'pending_review'" type="success" link size="small" @click="handleApproveTask(row)">通过</el-button>
                  <el-button v-if="row.status === 'pending_review'" type="danger" link size="small" @click="handleRejectTask(row)">驳回</el-button>
                  <el-button v-if="row.status === 'in_progress'" type="info" link size="small" @click="handleUpdateProgress(row)">进度</el-button>
                  <el-button v-if="row.delay_days > 0" type="warning" link size="small" @click="handleAddDelayReason(row)">延期原因</el-button>
                </template>
                <el-button v-if="!['completed','locked'].includes(row.status)" type="primary" link size="small" @click="handleEditGanttRow(row)">编辑</el-button>
                <el-button v-if="!['completed','locked'].includes(row.status)" type="danger" link size="small" @click="handleDeleteGantt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 日历视图 -->
          <div v-if="viewMode === 'calendar'" class="calendar-view">
            <div class="calendar-header">
              <el-button link @click="calendarMonth--">&#9664;</el-button>
              <span style="font-weight: bold; font-size: 15px">{{ calendarYear }}年{{ calendarMonth + 1 }}月</span>
              <el-button link @click="calendarMonth++">&#9654;</el-button>
            </div>
            <div class="calendar-grid">
              <div v-for="day in ['日','一','二','三','四','五','六']" :key="day" class="calendar-day-header">{{ day }}</div>
              <div v-for="(cell, idx) in calendarCells" :key="idx" class="calendar-cell" :class="{ 'other-month': !cell.isCurrentMonth, 'today': cell.isToday }">
                <div class="calendar-date">{{ cell.day }}</div>
                <div v-for="t in cell.tasks" :key="t.id" class="calendar-task" :class="{ 'task-overdue': t.delay_days > 0, 'task-critical': t.is_critical }" :title="t.task_name">
                  {{ t.task_type === 1 ? '&#9670;' : '' }} {{ t.task_name }}
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- ==================== 变更管理 ==================== -->
        <el-tab-pane label="变更管理" name="change">
          <div style="margin-bottom: 12px"><el-button type="primary" @click="handleAddChange">新建变更单</el-button></div>
          <el-table :data="changeData" v-loading="changeLoading" stripe border>
            <el-table-column prop="change_no" label="编号" width="130" />
            <el-table-column label="项目" width="120"><template #default="{ row }">{{ projectNameMap[row.project_id] || row.project_id }}</template></el-table-column>
            <el-table-column label="类型" width="100"><template #default="{ row }">{{ changeTypeLabel[row.change_type] || row.change_type }}</template></el-table-column>
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="total_amount" label="金额" width="120" align="right"><template #default="{ row }">{{ row.total_amount ? Number(row.total_amount).toLocaleString() : '-' }}</template></el-table-column>
            <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="changeStatusType[row.status]" size="small">{{ changeStatusLabel[row.status] || row.status }}</el-tag></template></el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="160" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditChange(row)">编辑</el-button>
                <el-dropdown @command="(cmd) => handleChangeStatusUpdate(row, cmd)" style="margin-left: 4px">
                  <el-button type="warning" link size="small">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                  <template #dropdown><el-dropdown-menu>
                    <el-dropdown-item command="pending">提交审批</el-dropdown-item>
                    <el-dropdown-item command="approved">通过</el-dropdown-item>
                    <el-dropdown-item command="rejected">驳回</el-dropdown-item>
                  </el-dropdown-menu></template>
                </el-dropdown>
                <el-button type="danger" link size="small" @click="handleDeleteChange(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="changeTotal > 0" style="margin-top: 16px; justify-content: flex-end" background layout="total, prev, pager, next" :total="changeTotal" v-model:current-page="changePage" @current-change="fetchChanges" />
        </el-tab-pane>

        <!-- ==================== 统计分析 ==================== -->
        <el-tab-pane label="统计分析" name="stats">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-card shadow="never"><h4>任务状态分布</h4>
                <div class="stat-bars">
                  <div v-for="(count, status) in statusDistEntries" :key="status" class="stat-bar-item">
                    <span class="stat-bar-label">{{ taskStatusLabel[status] || status }}</span>
                    <el-progress :percentage="statusDistTotal ? Math.round(count / statusDistTotal * 100) : 0" :stroke-width="18" :text-inside="true" :color="statusBarColor[status]" />
                    <span class="stat-bar-count">{{ count }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never"><h4>项目概况</h4>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="整体进度">{{ stats.overallProgress || 0 }}%</el-descriptions-item>
                  <el-descriptions-item label="关键路径节点">{{ stats.criticalPathCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="里程碑总数">{{ stats.milestoneTotal || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="里程碑达成">{{ stats.milestoneCompleted || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="达成率">{{ stats.milestoneAchievementRate || 0 }}%</el-descriptions-item>
                  <el-descriptions-item label="任务按期率">{{ stats.taskOnTimeRate || 0 }}%</el-descriptions-item>
                  <el-descriptions-item label="超期任务">{{ stats.overdueTaskCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="超期里程碑">{{ stats.overdueMilestoneCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="任务总数">{{ stats.taskTotal || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="已完成">{{ stats.taskCompleted || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="执行中">{{ stats.taskInProgress || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="待审核">{{ stats.taskPendingReview || 0 }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 里程碑对话框 ==================== -->
    <el-dialog v-model="msDialogVisible" :title="msDialogTitle" width="600px" @closed="resetMsForm">
      <el-form ref="msFormRef" :model="msForm" :rules="msRules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId"><el-select v-model="msForm.projectId" filterable placeholder="选择项目" style="width: 100%"><el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="里程碑名称" prop="milestoneName"><el-input v-model="msForm.milestoneName" placeholder="请输入" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划完成" prop="deadline"><el-date-picker v-model="msForm.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="实际完成"><el-date-picker v-model="msForm.actualEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="前置依赖"><el-select v-model="msForm.depMilestoneIds" multiple filterable clearable placeholder="选择前置里程碑" style="width: 100%"><el-option v-for="m in availableDepMilestones" :key="m.id" :label="m.task_name" :value="m.id" /></el-select></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="msForm.sortOrder" :min="0" controls-position="right" style="width: 160px" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="msDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitMilestone">确定</el-button></template>
    </el-dialog>

    <!-- ==================== WBS任务对话框 ==================== -->
    <el-dialog v-model="ganttDialogVisible" :title="ganttDialogTitle" width="800px" @closed="resetGanttForm">
      <el-form ref="ganttFormRef" :model="ganttForm" :rules="ganttRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="关联项目" prop="projectId"><el-select v-model="ganttForm.projectId" filterable style="width: 100%"><el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="任务类型" prop="taskType"><el-select v-model="ganttForm.taskType" style="width: 100%"><el-option label="里程碑" :value="1" /><el-option label="任务" :value="2" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="ganttForm.taskName" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="上级任务"><el-select v-model="ganttForm.parentId" filterable clearable style="width: 100%"><el-option v-for="m in allParentOptions" :key="m.id" :label="(m.wbs_code||'') + ' ' + m.task_name" :value="m.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人ID"><el-input-number v-model="ganttForm.assigneeId" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划开始"><el-date-picker v-model="ganttForm.planStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="计划结束"><el-date-picker v-model="ganttForm.planEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="权重"><el-input-number v-model="ganttForm.weight" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="WBS编码"><el-input v-model="ganttForm.wbsCode" placeholder="自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="关联合同ID"><el-input-number v-model="ganttForm.linkedContractId" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="前置依赖">
          <div style="width: 100%">
            <div v-for="(dep, idx) in ganttForm.dependencies" :key="idx" style="display: flex; gap: 8px; margin-bottom: 8px">
              <el-select v-model="dep.depTaskId" filterable clearable placeholder="前置任务" style="flex: 1"><el-option v-for="t in allTaskOptions" :key="t.id" :label="(t.wbs_code||'')+' '+t.task_name" :value="t.id" /></el-select>
              <el-select v-model="dep.depType" style="width: 80px"><el-option label="FS" value="FS" /><el-option label="SS" value="SS" /><el-option label="FF" value="FF" /><el-option label="SF" value="SF" /></el-select>
              <el-button type="danger" link @click="ganttForm.dependencies.splice(idx, 1)">删除</el-button>
            </div>
            <el-button type="primary" link @click="ganttForm.dependencies.push({ depTaskId: null, depType: 'FS' })">+ 添加依赖</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="ganttDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitGantt">确定</el-button></template>
    </el-dialog>

    <!-- ==================== 审批意见对话框 ==================== -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalDialogTitle" width="450px">
      <el-form label-width="80px"><el-form-item label="审批意见"><el-input v-model="approvalRemark" type="textarea" :rows="3" placeholder="审批意见(可选)" /></el-form-item></el-form>
      <template #footer><el-button @click="approvalDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="confirmApproval">确定</el-button></template>
    </el-dialog>

    <!-- ==================== 进度更新对话框 ==================== -->
    <el-dialog v-model="progressDialogVisible" title="更新任务进度" width="400px">
      <el-form label-width="80px"><el-form-item label="进度"><el-slider v-model="progressValue" :max="100" :step="1" show-input input-size="small" /></el-form-item></el-form>
      <template #footer><el-button @click="progressDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="confirmProgress">确定</el-button></template>
    </el-dialog>

    <!-- ==================== 延期原因对话框 ==================== -->
    <el-dialog v-model="delayDialogVisible" title="录入延期原因" width="550px">
      <el-form :model="delayForm" label-width="100px">
        <el-form-item label="原因分类"><el-select v-model="delayForm.reasonType" style="width: 100%">
          <el-option label="材料原因" value="material" /><el-option label="人工原因" value="labor" /><el-option label="设计变更" value="design" /><el-option label="天气原因" value="weather" /><el-option label="其他" value="other" />
        </el-select></el-form-item>
        <el-form-item label="延期原因"><el-input v-model="delayForm.reasonDetail" type="textarea" :rows="3" placeholder="请详细说明延期原因(必填)" /></el-form-item>
        <el-form-item label="关联整改任务"><el-select v-model="delayForm.rectifyTaskId" filterable clearable placeholder="选择关联整改任务(可选)" style="width: 100%"><el-option v-for="t in allTaskOptions" :key="t.id" :label="t.task_name" :value="t.id" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="delayDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitDelayReason">确定</el-button></template>
    </el-dialog>

    <!-- ==================== 预警面板 ==================== -->
    <el-drawer v-model="showWarningPanel" title="预警中心" size="500px">
      <el-empty v-if="!warnings.length" description="暂无预警" />
      <div v-for="w in warnings" :key="w.taskId + w.type" class="warning-item" :class="'warning-' + w.level">
        <el-tag :type="w.level === 'high' ? 'danger' : 'warning'" size="small">{{ w.type === 'overdue' ? '超期' : w.type === 'due_soon' ? '即将到期' : '风险' }}</el-tag>
        <span style="margin-left: 8px">{{ w.message }}</span>
        <div style="font-size: 11px; color: #999; margin-top: 2px">截止: {{ w.planEndDate }}</div>
      </div>
    </el-drawer>

    <!-- ==================== 审计日志面板 ==================== -->
    <el-drawer v-model="showAuditLog" title="操作日志" size="700px" @open="fetchAuditLogs">
      <el-table :data="auditLogs" stripe border size="small">
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="target_name" label="对象" width="140" show-overflow-tooltip />
        <el-table-column prop="field_name" label="字段" width="100" />
        <el-table-column prop="old_value" label="原值" width="100" show-overflow-tooltip />
        <el-table-column prop="new_value" label="新值" width="100" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="operator_id" label="操作人" width="80" />
        <el-table-column prop="operated_at" label="时间" width="155" />
      </el-table>
    </el-drawer>

    <!-- ==================== 导出预览 ==================== -->
    <el-dialog v-model="exportPreviewVisible" title="导出预览" width="90%">
      <div style="display: flex; gap: 8px; margin-bottom: 12px">
        <el-button type="primary" size="small" @click="printExport">打印/导出PDF</el-button>
      </div>
      <iframe ref="exportIframe" :srcdoc="exportHtml" style="width: 100%; height: 70vh; border: 1px solid #eee"></iframe>
    </el-dialog>

    <!-- ==================== 变更单对话框 ==================== -->
    <el-dialog v-model="changeDialogVisible" :title="isEditChange ? '编辑变更单' : '新建变更单'" width="850px" @closed="resetChangeForm">
      <el-form ref="changeFormRef" :model="changeForm" :rules="changeRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="关联项目" prop="projectId"><el-select v-model="changeForm.projectId" filterable style="width: 100%"><el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="变更类型" prop="changeType"><el-select v-model="changeForm.changeType" style="width: 100%"><el-option label="签证" value="visa" /><el-option label="业主变更" value="owner_change" /><el-option label="超量" value="overage" /><el-option label="劳务签证" value="labor_visa" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="变更标题" prop="title"><el-input v-model="changeForm.title" /></el-form-item>
        <el-form-item label="变更说明"><el-input v-model="changeForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left">变更明细</el-divider>
        <el-button type="primary" size="small" style="margin-bottom: 12px" @click="addChangeDetail">添加明细行</el-button>
        <el-table :data="changeForm.details" border size="small">
          <el-table-column label="项目名称" min-width="120"><template #default="{ row }"><el-input v-model="row.itemName" size="small" /></template></el-table-column>
          <el-table-column label="单位" width="70"><template #default="{ row }"><el-input v-model="row.unit" size="small" /></template></el-table-column>
          <el-table-column label="计划量" width="90"><template #default="{ row }"><el-input-number v-model="row.planQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
          <el-table-column label="实际量" width="90"><template #default="{ row }"><el-input-number v-model="row.actualQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
          <el-table-column label="单价" width="90"><template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
          <el-table-column label="小计" width="100"><template #default="{ row }">{{ (((row.actualQuantity||0)-(row.planQuantity||0))*(row.unitPrice||0)).toFixed(2) }}</template></el-table-column>
          <el-table-column label="操作" width="50"><template #default="{ $index }"><el-button type="danger" link size="small" @click="changeForm.details.splice($index, 1)">删</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <template #footer><el-button @click="changeDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitChange">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  getMilestoneList, getAllMilestones, createMilestone, updateMilestone, deleteMilestone,
  submitMilestoneApproval, approveMilestone, rejectMilestone, lockMilestone, completeMilestone,
  getGanttTree, createGanttTask, updateGanttTask, deleteGanttTask,
  startTask, submitTaskReview, approveTask, rejectTask, updateTaskProgress,
  getWarnings, addDelayReason, getAuditLogs, getProjectStats, getStatusDistribution, calcCriticalPath,
  exportProgressPlan, exportMilestoneReport, exportAcceptanceDoc,
  getChangeOrderList, createChangeOrder, updateChangeOrder, updateChangeOrderStatus, deleteChangeOrder, getChangeOrderDetails
} from '@/api/progress'
import { getAllProjects } from '@/api/project'

// ====== 常量 ======
const msStatusLabel = { draft: '草稿', pending: '审批中', approved: '已审批', locked: '已锁定', completed: '已完成', rejected: '已驳回' }
const msStatusTagType = { draft: 'info', pending: 'warning', approved: 'success', locked: '', completed: 'success', rejected: 'danger' }
const taskStatusLabel = { not_started: '未开始', in_progress: '执行中', pending_review: '待审核', completed: '已完成', rejected: '已驳回', draft: '草稿', pending: '审批中', approved: '已审批', locked: '已锁定' }
const taskStatusTagType = { not_started: 'info', in_progress: '', pending_review: 'warning', completed: 'success', rejected: 'danger', draft: 'info', pending: 'warning', approved: 'success', locked: '' }
const riskLabel = { low: '低', medium: '中', high: '高' }
const riskTagType = { low: 'success', medium: 'warning', high: 'danger' }
const statusBarColor = { not_started: '#909399', in_progress: '#409eff', pending_review: '#e6a23c', completed: '#67c23a', rejected: '#f56c6c' }
const changeTypeLabel = { visa: '签证', owner_change: '业主变更', overage: '超量', labor_visa: '劳务签证' }
const changeStatusLabel = { draft: '草稿', pending: '审批中', approved: '已审批', rejected: '已驳回' }
const changeStatusType = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger' }

// ====== 状态 ======
const loading = ref(false), changeLoading = ref(false), submitting = ref(false)
const activeTab = ref('milestone'), viewMode = ref('gantt')
const projects = ref([]), filterProjectId = ref(null), allMilestones = ref([])
const stats = ref({}), statusDist = ref({}), warnings = ref([]), auditLogs = ref([])
const showWarningPanel = ref(false), showAuditLog = ref(false)
const warningCount = computed(() => warnings.value.length)
const statusDistEntries = computed(() => { const d = statusDist.value; const r = {}; for (const k in d) { if (k !== '_total') r[k] = d[k] } return r })
const statusDistTotal = computed(() => Object.values(statusDistEntries.value).reduce((a, b) => a + b, 0))
const projectNameMap = computed(() => { const m = {}; projects.value.forEach(p => { m[p.id] = p.project_name || p.projectName }); return m })

// ====== 里程碑 ======
const milestoneData = ref([]), milestoneTotal = ref(0), milestonePage = ref(1)
const msDialogVisible = ref(false), msDialogTitle = ref('新建里程碑'), msFormRef = ref(null)
const isEditMs = ref(false), editMsId = ref(null)
const msForm = reactive({ projectId: null, milestoneName: '', deadline: null, actualEndDate: null, sortOrder: 0, depMilestoneIds: [] })
const msRules = { projectId: [{ required: true, message: '请选择项目', trigger: 'change' }], milestoneName: [{ required: true, message: '请输入名称', trigger: 'blur' }], deadline: [{ required: true, message: '请选择日期', trigger: 'change' }] }
const availableDepMilestones = computed(() => allMilestones.value.filter(m => m.id !== editMsId.value))

// ====== 甘特 ======
const ganttTreeData = ref([])
const flatGanttData = computed(() => { const r = []; const f = (list, lv) => { if (!list) return; for (const i of list) { r.push({ ...i, _level: lv }); if (i.children) f(i.children, lv + 1) } }; f(ganttTreeData.value, 0); return r })
const allParentOptions = computed(() => { const r = []; const c = (l) => { if (!l) return; for (const i of l) { r.push(i); if (i.children) c(i.children) } }; c(ganttTreeData.value); return r })
const allTaskOptions = computed(() => allParentOptions.value.filter(t => t.id !== editGanttId.value))

const ganttDialogVisible = ref(false), ganttDialogTitle = ref('新建任务'), ganttFormRef = ref(null)
const isEditGantt = ref(false), editGanttId = ref(null)
const ganttForm = reactive({ projectId: null, parentId: null, assigneeId: null, taskName: '', taskType: 2, planStartDate: null, planEndDate: null, weight: 1, wbsCode: '', sortOrder: 0, dependencies: [], linkedContractId: null })
const ganttRules = { projectId: [{ required: true, message: '请选择项目', trigger: 'change' }], taskName: [{ required: true, message: '请输入名称', trigger: 'blur' }], taskType: [{ required: true, message: '请选择类型', trigger: 'change' }] }

// ====== 审批/进度/延期对话框 ======
const approvalDialogVisible = ref(false), approvalDialogTitle = ref('审批'), approvalRemark = ref('')
let approvalCallback = null
const progressDialogVisible = ref(false), progressValue = ref(0)
let progressTaskId = null
const delayDialogVisible = ref(false)
const delayForm = reactive({ taskId: null, reasonType: 'other', reasonDetail: '', rectifyTaskId: null })

// ====== 导出 ======
const exportPreviewVisible = ref(false), exportHtml = ref(''), exportIframe = ref(null)

// ====== 变更单 ======
const changeData = ref([]), changeTotal = ref(0), changePage = ref(1)
const changeDialogVisible = ref(false), changeFormRef = ref(null), isEditChange = ref(false), editChangeId = ref(null)
const createEmptyDetail = () => ({ itemName: '', specModel: '', unit: '', planQuantity: null, actualQuantity: null, unitPrice: null })
const changeForm = reactive({ projectId: null, contractId: null, changeType: '', title: '', description: '', details: [createEmptyDetail()] })
const changeRules = { projectId: [{ required: true, message: '请选择项目', trigger: 'change' }], changeType: [{ required: true, message: '请选择类型', trigger: 'change' }], title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

// ====== 日历 ======
const today = new Date()
const calendarYear = ref(today.getFullYear()), calendarMonth = ref(today.getMonth())
watch(calendarMonth, (v) => { if (v < 0) { calendarMonth.value = 11; calendarYear.value-- } else if (v > 11) { calendarMonth.value = 0; calendarYear.value++ } })
const calendarCells = computed(() => {
  const y = calendarYear.value, m = calendarMonth.value
  const first = new Date(y, m, 1), last = new Date(y, m + 1, 0)
  const startDay = first.getDay(), totalDays = last.getDate()
  const cells = [], todayStr = new Date().toISOString().slice(0, 10)
  for (let i = 0; i < startDay; i++) { const d = new Date(y, m, -startDay + i + 1); cells.push({ day: d.getDate(), date: d.toISOString().slice(0, 10), isCurrentMonth: false, isToday: false, tasks: [] }) }
  for (let d = 1; d <= totalDays; d++) { const ds = `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`; cells.push({ day: d, date: ds, isCurrentMonth: true, isToday: ds === todayStr, tasks: [] }) }
  const rem = 7 - (cells.length % 7); if (rem < 7) { for (let i = 1; i <= rem; i++) { const d = new Date(y, m + 1, i); cells.push({ day: d.getDate(), date: d.toISOString().slice(0, 10), isCurrentMonth: false, isToday: false, tasks: [] }) } }
  flatGanttData.value.forEach(t => {
    const s = t.plan_start_date, e = t.plan_end_date
    if (!e) return
    cells.forEach(c => { if (c.date === e || c.date === s) c.tasks.push(t) })
  })
  return cells
})

// ====== 时间线计算 ======
const MS_NODE_W = 120, MS_GAP_X = 160, MS_GAP_Y = 120, MS_COLS = 5
const milestoneNodePos = (idx) => ({ x: 40 + (idx % MS_COLS) * MS_GAP_X, y: 20 + Math.floor(idx / MS_COLS) * MS_GAP_Y })
const timelineSvgWidth = computed(() => Math.max(Math.min(milestoneData.value.length, MS_COLS) * MS_GAP_X + 80, 400))
const timelineSvgHeight = computed(() => Math.max(Math.ceil(milestoneData.value.length / MS_COLS) * MS_GAP_Y + 40, 160))
const depLines = computed(() => { const lines = [], arr = milestoneData.value, map = {}; arr.forEach((ms, idx) => { map[ms.id] = idx }); arr.forEach((ms, idx) => { (ms.dep_milestone_ids || []).forEach(depId => { const di = map[depId]; if (di === undefined) return; const f = milestoneNodePos(di), t = milestoneNodePos(idx); lines.push({ x1: f.x + MS_NODE_W / 2, y1: f.y + 30, x2: t.x + MS_NODE_W / 2, y2: t.y }) }) }); return lines })

const ganttTimeRange = computed(() => { const a = flatGanttData.value; if (!a.length) return { min: null, max: null, days: 1 }; let mn = null, mx = null; a.forEach(t => { const s = t.plan_start_date ? new Date(t.plan_start_date) : null; const e = t.plan_end_date ? new Date(t.plan_end_date) : null; if (s && (!mn || s < mn)) mn = s; if (e && (!mx || e > mx)) mx = e }); if (!mn || !mx) return { min: null, max: null, days: 1 }; return { min: mn, max: mx, days: Math.max(1, Math.ceil((mx - mn) / 86400000) + 1) } })
const ganttBarStyle = (row) => { const { min, days } = ganttTimeRange.value; if (!min || !row.plan_start_date || !row.plan_end_date) return { display: 'none' }; const s = new Date(row.plan_start_date), e = new Date(row.plan_end_date); const so = Math.max(0, (s - min) / 86400000); const dur = Math.max(1, (e - s) / 86400000 + 1); return { left: (so / days * 100).toFixed(2) + '%', width: (dur / days * 100).toFixed(2) + '%', position: 'absolute', top: '4px', height: '20px' } }
const ganttRowClass = (row) => ({ 'gantt-milestone-row': row.task_type === 1, 'gantt-row-critical': row.is_critical, 'gantt-row-overdue': row.delay_days > 0 })

// ====== 数据加载 ======
const loadProjects = async () => { try { const res = await getAllProjects(); projects.value = res.data || [] } catch {} }
const loadAllMilestones = async () => { try { const params = {}; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getAllMilestones(params); allMilestones.value = res.data || [] } catch {} }
const fetchMilestones = async () => { loading.value = true; try { const params = { page: milestonePage.value, size: 20 }; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getMilestoneList(params); milestoneData.value = res.data.records || []; milestoneTotal.value = res.data.total || 0 } finally { loading.value = false } }
const fetchGanttTree = async () => { loading.value = true; try { const params = {}; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getGanttTree(params); ganttTreeData.value = res.data || [] } finally { loading.value = false } }
const fetchChanges = async () => { changeLoading.value = true; try { const params = { page: changePage.value, size: 20 }; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getChangeOrderList(params); changeData.value = res.data.records || []; changeTotal.value = res.data.total || 0 } finally { changeLoading.value = false } }
const fetchStats = async () => { try { const params = {}; if (filterProjectId.value) params.projectId = filterProjectId.value; const [s, d] = await Promise.all([getProjectStats(params), getStatusDistribution(params)]); stats.value = s.data || {}; statusDist.value = d.data || {} } catch {} }
const fetchWarnings = async () => { try { const params = {}; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getWarnings(params); warnings.value = res.data || [] } catch {} }
const fetchAuditLogs = async () => { try { const params = { page: 1, size: 50 }; if (filterProjectId.value) params.projectId = filterProjectId.value; const res = await getAuditLogs(params); auditLogs.value = res.data?.records || [] } catch {} }

const handleProjectChange = () => { milestonePage.value = 1; changePage.value = 1; fetchCurrentTab(); fetchStats(); fetchWarnings() }
const handleTabChange = () => { fetchCurrentTab() }
const fetchCurrentTab = () => { if (activeTab.value === 'milestone') { fetchMilestones(); loadAllMilestones() } else if (activeTab.value === 'gantt') { fetchGanttTree(); loadAllMilestones() } else if (activeTab.value === 'change') fetchChanges(); else if (activeTab.value === 'stats') fetchStats() }

// ====== 里程碑 CRUD ======
const handleAddMilestone = () => { isEditMs.value = false; editMsId.value = null; msDialogTitle.value = '新建里程碑'; if (filterProjectId.value) msForm.projectId = filterProjectId.value; loadAllMilestones(); msDialogVisible.value = true }
const handleEditMilestone = (row) => { isEditMs.value = true; editMsId.value = row.id; msDialogTitle.value = '编辑里程碑'; Object.assign(msForm, { projectId: row.project_id, milestoneName: row.task_name, deadline: row.plan_end_date, actualEndDate: row.actual_end_date || null, sortOrder: row.sort_order || 0, depMilestoneIds: row.dep_milestone_ids || [] }); loadAllMilestones(); msDialogVisible.value = true }
const resetMsForm = () => { Object.assign(msForm, { projectId: null, milestoneName: '', deadline: null, actualEndDate: null, sortOrder: 0, depMilestoneIds: [] }); msFormRef.value?.resetFields() }
const submitMilestone = async () => { await msFormRef.value.validate(); submitting.value = true; try { if (isEditMs.value) { await updateMilestone(editMsId.value, msForm); ElMessage.success('更新成功') } else { await createMilestone(msForm); ElMessage.success('创建成功') }; msDialogVisible.value = false; fetchMilestones(); loadAllMilestones(); fetchStats() } finally { submitting.value = false } }
const handleDeleteMilestone = async (row) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteMilestone(row.id); ElMessage.success('删除成功'); fetchMilestones(); loadAllMilestones(); fetchStats() }
const handleMsSubmitApproval = async (row) => { await ElMessageBox.confirm('确定提交审批？', '提示'); await submitMilestoneApproval(row.id); ElMessage.success('已提交'); fetchMilestones() }
const handleMsApprove = (row) => { approvalDialogTitle.value = '审批通过 — ' + row.task_name; approvalRemark.value = ''; approvalCallback = async () => { await approveMilestone(row.id, { remark: approvalRemark.value }); ElMessage.success('通过'); fetchMilestones() }; approvalDialogVisible.value = true }
const handleMsReject = (row) => { approvalDialogTitle.value = '驳回 — ' + row.task_name; approvalRemark.value = ''; approvalCallback = async () => { await rejectMilestone(row.id, { remark: approvalRemark.value }); ElMessage.success('已驳回'); fetchMilestones() }; approvalDialogVisible.value = true }
const handleMsLock = async (row) => { await ElMessageBox.confirm('锁定后不可编辑，确定？', '提示', { type: 'warning' }); await lockMilestone(row.id); ElMessage.success('已锁定'); fetchMilestones() }
const handleMsComplete = (row) => { approvalDialogTitle.value = '完成里程碑 — ' + row.task_name; approvalRemark.value = ''; approvalCallback = async () => { await completeMilestone(row.id, { remark: approvalRemark.value }); ElMessage.success('已完成'); fetchMilestones(); fetchStats() }; approvalDialogVisible.value = true }

// ====== 甘特任务 CRUD ======
const handleAddGantt = () => { isEditGantt.value = false; editGanttId.value = null; ganttForm.taskType = 2; ganttDialogTitle.value = '新建任务'; if (filterProjectId.value) ganttForm.projectId = filterProjectId.value; ganttDialogVisible.value = true }
const handleEditGanttRow = (row) => { isEditGantt.value = true; editGanttId.value = row.id; ganttDialogTitle.value = row.task_type === 1 ? '编辑里程碑' : '编辑任务'; Object.assign(ganttForm, { projectId: row.project_id, parentId: row.parent_id || null, assigneeId: row.assignee_id || null, taskName: row.task_name || '', taskType: row.task_type, planStartDate: row.plan_start_date || null, planEndDate: row.plan_end_date || null, weight: row.weight || 1, wbsCode: row.wbs_code || '', sortOrder: row.sort_order || 0, dependencies: (row.dependencies || []).map(d => ({ depTaskId: d.dep_task_id, depType: d.dep_type || 'FS' })), linkedContractId: row.linked_contract_id || null }); ganttDialogVisible.value = true }
const resetGanttForm = () => { Object.assign(ganttForm, { projectId: null, parentId: null, assigneeId: null, taskName: '', taskType: 2, planStartDate: null, planEndDate: null, weight: 1, wbsCode: '', sortOrder: 0, dependencies: [], linkedContractId: null }); ganttFormRef.value?.resetFields() }
const submitGantt = async () => { await ganttFormRef.value.validate(); const payload = { ...ganttForm, dependencies: ganttForm.dependencies.filter(d => d.depTaskId) }; submitting.value = true; try { if (isEditGantt.value) { await updateGanttTask(editGanttId.value, payload); ElMessage.success('更新成功') } else { await createGanttTask(payload); ElMessage.success('创建成功') }; ganttDialogVisible.value = false; fetchGanttTree(); loadAllMilestones(); fetchStats() } finally { submitting.value = false } }
const handleDeleteGantt = async (row) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteGanttTask(row.id); ElMessage.success('删除成功'); fetchGanttTree(); fetchStats() }

// ====== 任务状态流转 ======
const handleStartTask = async (row) => { try { await startTask(row.id); ElMessage.success('已开始'); fetchGanttTree(); fetchStats() } catch (e) { ElMessage.error(e.response?.data?.message || '失败') } }
const handleSubmitReview = async (row) => { await ElMessageBox.confirm('确定提交审核？'); await submitTaskReview(row.id); ElMessage.success('已提交'); fetchGanttTree() }
const handleApproveTask = (row) => { approvalDialogTitle.value = '通过 — ' + row.task_name; approvalRemark.value = ''; approvalCallback = async () => { await approveTask(row.id, { remark: approvalRemark.value }); ElMessage.success('通过'); fetchGanttTree(); fetchStats() }; approvalDialogVisible.value = true }
const handleRejectTask = (row) => { approvalDialogTitle.value = '驳回 — ' + row.task_name; approvalRemark.value = ''; approvalCallback = async () => { await rejectTask(row.id, { remark: approvalRemark.value }); ElMessage.success('已驳回'); fetchGanttTree() }; approvalDialogVisible.value = true }
const handleUpdateProgress = (row) => { progressTaskId = row.id; progressValue.value = Number(row.progress_pct || 0); progressDialogVisible.value = true }
const confirmProgress = async () => { submitting.value = true; try { await updateTaskProgress(progressTaskId, progressValue.value); ElMessage.success('已更新'); progressDialogVisible.value = false; fetchGanttTree(); fetchStats() } finally { submitting.value = false } }
const confirmApproval = async () => { if (!approvalCallback) return; submitting.value = true; try { await approvalCallback(); approvalDialogVisible.value = false } catch (e) { ElMessage.error(e.response?.data?.message || '失败') } finally { submitting.value = false } }

// ====== 延期原因 ======
const handleAddDelayReason = (row) => { Object.assign(delayForm, { taskId: row.id, reasonType: 'other', reasonDetail: '', rectifyTaskId: null }); delayDialogVisible.value = true }
const submitDelayReason = async () => { if (!delayForm.reasonDetail) { ElMessage.warning('请填写延期原因'); return }; submitting.value = true; try { await addDelayReason(delayForm); ElMessage.success('已记录'); delayDialogVisible.value = false } finally { submitting.value = false } }

// ====== 关键路径 ======
const handleCalcCriticalPath = async () => { try { await calcCriticalPath(filterProjectId.value); ElMessage.success('关键路径已计算'); fetchGanttTree(); fetchStats() } catch (e) { ElMessage.error(e.response?.data?.message || '计算失败') } }

// ====== 导出 ======
const handleExportProgressPlan = async () => { try { const pn = filterProjectId.value ? projectNameMap.value[filterProjectId.value] : '全部项目'; const res = await exportProgressPlan({ projectId: filterProjectId.value, projectName: pn }); exportHtml.value = res.data; exportPreviewVisible.value = true } catch (e) { ElMessage.error(e.response?.data?.message || '导出失败') } }
const handleExportMilestoneReport = async () => { try { const pn = filterProjectId.value ? projectNameMap.value[filterProjectId.value] : '全部项目'; const res = await exportMilestoneReport({ projectId: filterProjectId.value, projectName: pn }); exportHtml.value = res.data; exportPreviewVisible.value = true } catch (e) { ElMessage.error(e.response?.data?.message || '导出失败') } }
const handleExportAcceptance = async (row) => { try { const res = await exportAcceptanceDoc(row.id); exportHtml.value = res.data; exportPreviewVisible.value = true } catch (e) { ElMessage.error(e.response?.data?.message || '导出失败') } }
const printExport = () => { const iframe = exportIframe.value; if (iframe?.contentWindow) iframe.contentWindow.print() }

// ====== 变更单 ======
const handleAddChange = () => { isEditChange.value = false; editChangeId.value = null; changeDialogVisible.value = true }
const handleEditChange = async (row) => { isEditChange.value = true; editChangeId.value = row.id; let details = [createEmptyDetail()]; try { const res = await getChangeOrderDetails(row.id); const d = res.data || []; if (d.length) details = d.map(x => ({ itemName: x.item_name || '', specModel: x.spec_model || '', unit: x.unit || '', planQuantity: x.plan_quantity, actualQuantity: x.actual_quantity, unitPrice: x.unit_price })) } catch {}; Object.assign(changeForm, { projectId: row.project_id, contractId: row.contract_id || null, changeType: row.change_type || '', title: row.title || '', description: row.description || '', details }); changeDialogVisible.value = true }
const resetChangeForm = () => { Object.assign(changeForm, { projectId: null, contractId: null, changeType: '', title: '', description: '', details: [createEmptyDetail()] }); changeFormRef.value?.resetFields() }
const addChangeDetail = () => { changeForm.details.push(createEmptyDetail()) }
const submitChange = async () => { await changeFormRef.value.validate(); const details = changeForm.details.filter(d => d.itemName).map(d => { const diff = (d.actualQuantity || 0) - (d.planQuantity || 0); return { ...d, diffQuantity: diff, subtotal: diff * (d.unitPrice || 0) } }); const totalAmount = details.reduce((s, d) => s + (d.subtotal || 0), 0); const payload = { ...changeForm, totalAmount, details }; submitting.value = true; try { if (isEditChange.value) { await updateChangeOrder(editChangeId.value, payload); ElMessage.success('更新成功') } else { await createChangeOrder(payload); ElMessage.success('创建成功') }; changeDialogVisible.value = false; fetchChanges() } finally { submitting.value = false } }
const handleChangeStatusUpdate = async (row, status) => { await ElMessageBox.confirm(`确定改为"${changeStatusLabel[status]}"？`, '提示', { type: 'warning' }); await updateChangeOrderStatus(row.id, status); ElMessage.success('已更新'); fetchChanges() }
const handleDeleteChange = async (row) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteChangeOrder(row.id); ElMessage.success('已删除'); fetchChanges() }

onMounted(() => { loadProjects(); loadAllMilestones(); fetchMilestones(); fetchStats(); fetchWarnings() })
</script>

<style scoped>
.stat-card { text-align: center; padding: 8px 0; }
.stat-card .stat-val { font-size: 22px; font-weight: bold; color: #303133; }
.stat-card .stat-lbl { font-size: 12px; color: #909399; margin-top: 2px; }
.stat-card.warn .stat-val { color: #f56c6c; }
.text-danger { color: #f56c6c; font-weight: bold; }
.milestone-timeline-wrapper { overflow-x: auto; border: 1px solid var(--el-border-color-lighter); border-radius: 6px; background: var(--el-fill-color-extra-light); padding: 12px; }
.milestone-timeline { position: relative; min-height: 160px; }
.milestone-dep-lines { position: absolute; top: 0; left: 0; pointer-events: none; }
.milestone-node { position: absolute; width: 120px; text-align: center; }
.milestone-diamond { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; }
.milestone-icon { font-size: 28px; line-height: 1; }
.ms-draft .milestone-icon { color: #909399; }
.ms-pending .milestone-icon { color: #e6a23c; }
.ms-approved .milestone-icon { color: #67c23a; }
.ms-locked .milestone-icon { color: #303133; }
.ms-completed .milestone-icon { color: #409eff; }
.ms-overdue .milestone-icon { color: #f56c6c !important; }
.milestone-label { font-size: 12px; font-weight: 600; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.milestone-date { font-size: 11px; color: #909399; margin-top: 1px; }
.gantt-chart-wrapper { overflow-x: auto; border: 1px solid var(--el-border-color-lighter); border-radius: 4px; }
.gantt-chart { width: 100%; border-collapse: collapse; }
.gantt-chart th { background: var(--el-fill-color-light); border-bottom: 1px solid var(--el-border-color); font-size: 13px; font-weight: 500; }
.gantt-chart td { border-bottom: 1px solid var(--el-border-color-extra-light); font-size: 13px; }
.gantt-milestone-row { background: var(--el-color-primary-light-9); }
.gantt-row-critical { background: #fffbe6; }
.gantt-row-overdue { background: #fff2f0; }
.gantt-bar-container { position: relative; height: 28px; background: var(--el-fill-color-lighter); border-radius: 4px; }
.gantt-bar { background: var(--el-color-primary-light-5); border-radius: 3px; overflow: hidden; cursor: pointer; }
.gantt-bar-critical { background: #e6a23c; }
.gantt-bar-overdue { background: #f56c6c; }
.gantt-bar-fill { height: 100%; background: var(--el-color-primary); border-radius: 3px; transition: width 0.3s; }
.warning-item { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; }
.warning-high { border-left: 3px solid #f56c6c; }
.warning-medium { border-left: 3px solid #e6a23c; }
.stat-bars { padding: 8px 0; }
.stat-bar-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.stat-bar-label { width: 60px; font-size: 12px; text-align: right; }
.stat-bar-count { width: 30px; font-size: 12px; }
.stat-bar-item .el-progress { flex: 1; }
.calendar-view { border: 1px solid var(--el-border-color-lighter); border-radius: 4px; overflow: hidden; }
.calendar-header { display: flex; align-items: center; justify-content: center; gap: 16px; padding: 8px; background: var(--el-fill-color-light); }
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); }
.calendar-day-header { text-align: center; font-size: 12px; font-weight: bold; padding: 6px; background: var(--el-fill-color-extra-light); border-bottom: 1px solid var(--el-border-color-extra-light); }
.calendar-cell { min-height: 80px; padding: 4px; border: 1px solid var(--el-border-color-extra-light); font-size: 11px; }
.calendar-cell.other-month { background: var(--el-fill-color-extra-light); opacity: 0.5; }
.calendar-cell.today { background: #ecf5ff; }
.calendar-date { font-weight: bold; margin-bottom: 2px; }
.calendar-task { padding: 1px 4px; margin-bottom: 1px; border-radius: 2px; background: var(--el-color-primary-light-8); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 10px; }
.calendar-task.task-overdue { background: #fde2e2; color: #f56c6c; }
.calendar-task.task-critical { background: #fdf6ec; color: #e6a23c; }
</style>
