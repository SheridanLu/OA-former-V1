<template>
  <div class="progress-page">
    <el-card shadow="never">
      <!-- 全局项目筛选 -->
      <el-form inline style="margin-bottom: 0">
        <el-form-item label="关联项目">
          <el-select v-model="filterProjectId" filterable clearable placeholder="全部项目" style="width: 220px" @change="handleProjectChange">
            <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 里程碑管理 ==================== -->
        <el-tab-pane label="里程碑管理" name="milestone">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddMilestone">新建里程碑</el-button>
          </div>

          <!-- 里程碑时间线可视化 -->
          <div v-if="milestoneData.length > 0" class="milestone-timeline-wrapper">
            <div class="milestone-timeline">
              <svg :width="timelineSvgWidth" :height="timelineSvgHeight" class="milestone-dep-lines">
                <defs><marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#409eff" /></marker></defs>
                <line v-for="(line, idx) in depLines" :key="idx"
                  :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2"
                  stroke="#409eff" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#arrowhead)" />
              </svg>
              <div v-for="(ms, idx) in milestoneData" :key="ms.id" class="milestone-node"
                :style="{ left: milestoneNodePos(idx).x + 'px', top: milestoneNodePos(idx).y + 'px' }">
                <div class="milestone-diamond" :class="'ms-' + (ms.status || 'draft')" :title="ms.task_name">
                  <span class="milestone-icon">&#9670;</span>
                </div>
                <div class="milestone-label">{{ ms.task_name }}</div>
                <div class="milestone-date">{{ ms.plan_end_date || '未设置' }}</div>
                <el-tag :type="statusTagType[ms.status]" size="small" style="margin-top: 2px">{{ statusLabel[ms.status] || ms.status }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 里程碑列表 -->
          <el-table :data="milestoneData" v-loading="loading" stripe border style="margin-top: 12px">
            <el-table-column prop="task_name" label="里程碑名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="关联项目" width="140">
              <template #default="{ row }">{{ projectNameMap[row.project_id] || row.project_id }}</template>
            </el-table-column>
            <el-table-column prop="plan_end_date" label="计划完成时间" width="130" />
            <el-table-column prop="actual_end_date" label="实际完成时间" width="130" />
            <el-table-column label="前置依赖" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.dep_milestone_names && row.dep_milestone_names.length">
                  <el-tag v-for="name in row.dep_milestone_names" :key="name" size="small" type="info" style="margin: 0 4px 2px 0">{{ name }}</el-tag>
                </template>
                <span v-else style="color: #999">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditMilestone(row)">编辑</el-button>
                <el-dropdown @command="(cmd) => handleMilestoneStatusChange(row, cmd)" style="margin-left: 4px">
                  <el-button type="warning" link size="small">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="pending" :disabled="row.status === 'pending'">提交审批</el-dropdown-item>
                      <el-dropdown-item command="approved" :disabled="row.status === 'approved'">审批通过</el-dropdown-item>
                      <el-dropdown-item command="locked" :disabled="row.status === 'locked'">锁定</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button type="danger" link size="small" @click="handleDeleteMilestone(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="milestoneTotal > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="milestoneTotal"
            v-model:current-page="milestonePage" @current-change="fetchMilestones" />
        </el-tab-pane>

        <!-- ==================== 甘特图管理 ==================== -->
        <el-tab-pane label="甘特图管理" name="gantt">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddGantt">新建任务</el-button>
          </div>

          <!-- 甘特图可视化 -->
          <div v-if="ganttData.length > 0" class="gantt-chart-wrapper">
            <table class="gantt-chart" border="0" cellspacing="0">
              <thead>
                <tr>
                  <th style="min-width: 200px; text-align: left; padding: 6px 8px">任务名称</th>
                  <th style="width: 80px">进度</th>
                  <th style="width: 90px">状态</th>
                  <th style="min-width: 400px; text-align: left; padding: 6px 8px">时间线</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in ganttData" :key="row.id" :class="{ 'gantt-milestone-row': row.task_type === 1 }">
                  <td style="padding: 6px 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                    <span v-if="row.task_type === 1" style="font-weight: bold">◆ {{ row.task_name }}</span>
                    <span v-else style="padding-left: 16px">├ {{ row.task_name }}</span>
                  </td>
                  <td style="text-align: center">{{ row.progress_pct }}%</td>
                  <td style="text-align: center">
                    <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] || row.status }}</el-tag>
                  </td>
                  <td style="padding: 6px 8px">
                    <div class="gantt-bar-container">
                      <div class="gantt-bar"
                        :style="ganttBarStyle(row)"
                        :title="`${row.plan_start_date || '?'} ~ ${row.plan_end_date || '?'}`">
                        <div class="gantt-bar-fill" :style="{ width: (row.progress_pct || 0) + '%' }"></div>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 详细列表 -->
          <el-table :data="ganttData" v-loading="loading" stripe border style="margin-top: 12px">
            <el-table-column label="任务名称" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.task_type === 1" style="font-weight: bold">◆ {{ row.task_name }}</span>
                <span v-else style="padding-left: 16px">{{ row.task_name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="所属里程碑" width="140">
              <template #default="{ row }">{{ row.parent_id ? milestoneNameMap[row.parent_id] || row.parent_id : '-' }}</template>
            </el-table-column>
            <el-table-column prop="plan_start_date" label="计划开始" width="110" />
            <el-table-column prop="plan_end_date" label="计划结束" width="110" />
            <el-table-column prop="actual_start_date" label="实际开始" width="110" />
            <el-table-column prop="actual_end_date" label="实际结束" width="110" />
            <el-table-column prop="progress_pct" label="进度(%)" width="85" align="right" />
            <el-table-column label="依赖" width="100">
              <template #default="{ row }">
                <span v-if="row.dependency_task_id">{{ row.dependency_type || 'FS' }} #{{ row.dependency_task_id }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditGanttRow(row)">编辑</el-button>
                <el-dropdown @command="(cmd) => handleStatusChange(row, cmd)" style="margin-left: 4px">
                  <el-button type="warning" link size="small">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="pending">提交审批</el-dropdown-item>
                      <el-dropdown-item command="approved">审批通过</el-dropdown-item>
                      <el-dropdown-item command="locked">锁定</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button type="danger" link size="small" @click="handleDeleteGantt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="ganttTotal > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="ganttTotal"
            v-model:current-page="ganttPage" @current-change="fetchGanttTasks" />
        </el-tab-pane>

        <!-- ==================== 变更管理 ==================== -->
        <el-tab-pane label="变更管理" name="change">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddChange">新建变更单</el-button>
          </div>
          <el-table :data="changeData" v-loading="changeLoading" stripe border>
            <el-table-column prop="change_no" label="变更编号" width="140" />
            <el-table-column label="关联项目" width="120">
              <template #default="{ row }">{{ projectNameMap[row.project_id] || row.project_id }}</template>
            </el-table-column>
            <el-table-column label="变更类型" width="110">
              <template #default="{ row }">{{ changeTypeLabel[row.change_type] || row.change_type }}</template>
            </el-table-column>
            <el-table-column prop="title" label="变更标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="total_amount" label="变更金额" width="130" align="right">
              <template #default="{ row }">{{ row.total_amount ? Number(row.total_amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="changeStatusType[row.status]" size="small">{{ changeStatusLabel[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditChange(row)">编辑</el-button>
                <el-dropdown @command="(cmd) => handleChangeStatusUpdate(row, cmd)" style="margin-left: 4px">
                  <el-button type="warning" link size="small">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="pending">提交审批</el-dropdown-item>
                      <el-dropdown-item command="approved">审批通过</el-dropdown-item>
                      <el-dropdown-item command="rejected">驳回</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button type="danger" link size="small" @click="handleDeleteChange(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="changeTotal > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="changeTotal"
            v-model:current-page="changePage" @current-change="fetchChanges" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 里程碑对话框 ==================== -->
    <el-dialog v-model="msDialogVisible" :title="msDialogTitle" width="600px" @closed="resetMsForm">
      <el-form ref="msFormRef" :model="msForm" :rules="msRules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId">
          <el-select v-model="msForm.projectId" filterable placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="里程碑名称" prop="milestoneName">
          <el-input v-model="msForm.milestoneName" placeholder="请输入里程碑名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划完成时间" prop="deadline">
              <el-date-picker v-model="msForm.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际完成时间">
              <el-date-picker v-model="msForm.actualEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="前置依赖">
          <el-select v-model="msForm.depMilestoneIds" multiple filterable clearable placeholder="选择前置里程碑" style="width: 100%">
            <el-option v-for="m in availableDepMilestones" :key="m.id" :label="m.task_name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="msForm.sortOrder" :min="0" controls-position="right" style="width: 160px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="msDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitMilestone">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 甘特任务对话框 ==================== -->
    <el-dialog v-model="ganttDialogVisible" :title="ganttDialogTitle" width="750px" @closed="resetGanttForm">
      <el-form ref="ganttFormRef" :model="ganttForm" :rules="ganttRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="ganttForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="ganttForm.taskType" style="width: 100%">
                <el-option label="里程碑" :value="1" />
                <el-option label="任务" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="ganttForm.taskName" />
        </el-form-item>
        <el-row :gutter="16" v-if="ganttForm.taskType === 2">
          <el-col :span="12">
            <el-form-item label="所属里程碑">
              <el-select v-model="ganttForm.parentId" filterable clearable placeholder="选择里程碑" style="width: 100%">
                <el-option v-for="m in allMilestones" :key="m.id" :label="m.task_name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="依赖类型">
              <el-select v-model="ganttForm.dependencyType" clearable placeholder="类型" style="width: 100%">
                <el-option label="FS" value="FS" />
                <el-option label="SS" value="SS" />
                <el-option label="FF" value="FF" />
                <el-option label="SF" value="SF" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="依赖任务ID">
              <el-input-number v-model="ganttForm.dependencyTaskId" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划开始">
              <el-date-picker v-model="ganttForm.planStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束">
              <el-date-picker v-model="ganttForm.planEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际开始">
              <el-date-picker v-model="ganttForm.actualStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际结束">
              <el-date-picker v-model="ganttForm.actualEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="进度(%)">
              <el-slider v-model="ganttForm.progressPct" :max="100" :step="1" show-input input-size="small" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序号">
              <el-input-number v-model="ganttForm.sortOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="ganttDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitGantt">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 变更单对话框 ==================== -->
    <el-dialog v-model="changeDialogVisible" :title="isEditChange ? '编辑变更单' : '新建变更单'" width="850px" @closed="resetChangeForm">
      <el-form ref="changeFormRef" :model="changeForm" :rules="changeRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="changeForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更类型" prop="changeType">
              <el-select v-model="changeForm.changeType" style="width: 100%">
                <el-option label="签证" value="visa" />
                <el-option label="业主变更" value="owner_change" />
                <el-option label="超量" value="overage" />
                <el-option label="劳务签证" value="labor_visa" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联合同">
              <el-input-number v-model="changeForm.contractId" :min="1" controls-position="right" placeholder="合同ID" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="变更标题" prop="title">
          <el-input v-model="changeForm.title" />
        </el-form-item>
        <el-form-item label="变更说明">
          <el-input v-model="changeForm.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider content-position="left">变更明细</el-divider>
        <el-button type="primary" size="small" style="margin-bottom: 12px" @click="addChangeDetail">添加明细行</el-button>
        <el-table :data="changeForm.details" border size="small">
          <el-table-column label="项目名称" min-width="120">
            <template #default="{ row }"><el-input v-model="row.itemName" size="small" /></template>
          </el-table-column>
          <el-table-column label="规格型号" width="100">
            <template #default="{ row }"><el-input v-model="row.specModel" size="small" /></template>
          </el-table-column>
          <el-table-column label="单位" width="70">
            <template #default="{ row }"><el-input v-model="row.unit" size="small" /></template>
          </el-table-column>
          <el-table-column label="计划量" width="90">
            <template #default="{ row }"><el-input-number v-model="row.planQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="实际量" width="90">
            <template #default="{ row }"><el-input-number v-model="row.actualQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">{{ (((row.actualQuantity || 0) - (row.planQuantity || 0)) * (row.unitPrice || 0)).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="changeForm.details.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="changeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitChange">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  getMilestoneList, getAllMilestones, createMilestone, updateMilestone, deleteMilestone,
  getGanttTaskList, createGanttTask, updateGanttTask, updateGanttTaskStatus, deleteGanttTask,
  getChangeOrderList, createChangeOrder, updateChangeOrder, updateChangeOrderStatus, deleteChangeOrder,
  getChangeOrderDetails
} from '@/api/progress'
import { getAllProjects } from '@/api/project'

// ====== 常量 ======
const statusLabel = { draft: '草稿', pending: '审批中', approved: '已审批', locked: '已锁定' }
const statusTagType = { draft: 'info', pending: 'warning', approved: 'success', locked: '' }
const changeTypeLabel = { visa: '签证', owner_change: '业主变更', overage: '超量', labor_visa: '劳务签证' }
const changeStatusLabel = { draft: '草稿', pending: '审批中', approved: '已审批', rejected: '已驳回' }
const changeStatusType = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger' }

// ====== 共享状态 ======
const loading = ref(false)
const changeLoading = ref(false)
const submitting = ref(false)
const activeTab = ref('milestone')
const projects = ref([])
const filterProjectId = ref(null)
const allMilestones = ref([])

// 项目名称映射
const projectNameMap = computed(() => {
  const map = {}
  projects.value.forEach(p => { map[p.id] = p.project_name || p.projectName })
  return map
})

// 里程碑名称映射
const milestoneNameMap = computed(() => {
  const map = {}
  allMilestones.value.forEach(m => { map[m.id] = m.task_name })
  return map
})

// ====== 里程碑数据 ======
const milestoneData = ref([])
const milestoneTotal = ref(0)
const milestonePage = ref(1)

// ====== 里程碑表单 ======
const msDialogVisible = ref(false)
const msDialogTitle = ref('新建里程碑')
const msFormRef = ref(null)
const isEditMs = ref(false)
const editMsId = ref(null)

const msForm = reactive({
  projectId: null, milestoneName: '', deadline: null, actualEndDate: null,
  sortOrder: 0, depMilestoneIds: []
})

const msRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  milestoneName: [{ required: true, message: '请输入里程碑名称', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择计划完成时间', trigger: 'change' }]
}

// 可选的依赖里程碑（排除自身）
const availableDepMilestones = computed(() => {
  return allMilestones.value.filter(m => m.id !== editMsId.value)
})

// ====== 甘特图数据 ======
const ganttData = ref([])
const ganttTotal = ref(0)
const ganttPage = ref(1)

// ====== 变更单数据 ======
const changeData = ref([])
const changeTotal = ref(0)
const changePage = ref(1)

// ====== 甘特任务表单 ======
const ganttDialogVisible = ref(false)
const ganttDialogTitle = ref('新建里程碑')
const ganttFormRef = ref(null)
const isEditGantt = ref(false)
const editGanttId = ref(null)

const ganttForm = reactive({
  projectId: null, parentId: null, taskName: '', taskType: 1,
  planStartDate: null, planEndDate: null, actualStartDate: null, actualEndDate: null,
  progressPct: 0, sortOrder: 0, dependencyType: null, dependencyTaskId: null
})

const ganttRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }]
}

// ====== 变更单表单 ======
const changeDialogVisible = ref(false)
const changeFormRef = ref(null)
const isEditChange = ref(false)
const editChangeId = ref(null)

const createEmptyDetail = () => ({ itemName: '', specModel: '', unit: '', planQuantity: null, actualQuantity: null, unitPrice: null })
const changeForm = reactive({
  projectId: null, contractId: null, changeType: '', title: '', description: '',
  details: [createEmptyDetail()]
})
const changeRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入变更标题', trigger: 'blur' }]
}

// ====== 里程碑时间线计算 ======
const MS_NODE_W = 120
const MS_NODE_H = 100
const MS_GAP_X = 160
const MS_GAP_Y = 120
const MS_COLS = 5

const milestoneNodePos = (idx) => {
  const col = idx % MS_COLS
  const row = Math.floor(idx / MS_COLS)
  return { x: 40 + col * MS_GAP_X, y: 20 + row * MS_GAP_Y }
}

const timelineSvgWidth = computed(() => {
  const count = milestoneData.value.length
  const cols = Math.min(count, MS_COLS)
  return Math.max(cols * MS_GAP_X + 80, 400)
})

const timelineSvgHeight = computed(() => {
  const count = milestoneData.value.length
  const rows = Math.ceil(count / MS_COLS)
  return Math.max(rows * MS_GAP_Y + 40, 160)
})

const depLines = computed(() => {
  const lines = []
  const dataArr = milestoneData.value
  const idxMap = {}
  dataArr.forEach((ms, idx) => { idxMap[ms.id] = idx })

  dataArr.forEach((ms, idx) => {
    const deps = ms.dep_milestone_ids || []
    deps.forEach(depId => {
      const depIdx = idxMap[depId]
      if (depIdx === undefined) return
      const from = milestoneNodePos(depIdx)
      const to = milestoneNodePos(idx)
      lines.push({
        x1: from.x + MS_NODE_W / 2,
        y1: from.y + 30,
        x2: to.x + MS_NODE_W / 2,
        y2: to.y
      })
    })
  })
  return lines
})

// ====== 甘特图时间线计算 ======
const ganttTimeRange = computed(() => {
  const allTasks = ganttData.value
  if (!allTasks.length) return { min: null, max: null, days: 1 }
  let min = null, max = null
  allTasks.forEach(t => {
    const s = t.plan_start_date ? new Date(t.plan_start_date) : null
    const e = t.plan_end_date ? new Date(t.plan_end_date) : null
    if (s && (!min || s < min)) min = s
    if (e && (!max || e > max)) max = e
  })
  if (!min || !max) return { min: null, max: null, days: 1 }
  const days = Math.max(1, Math.ceil((max - min) / 86400000) + 1)
  return { min, max, days }
})

const ganttBarStyle = (row) => {
  const { min, days } = ganttTimeRange.value
  if (!min || !row.plan_start_date || !row.plan_end_date) return { display: 'none' }
  const s = new Date(row.plan_start_date)
  const e = new Date(row.plan_end_date)
  const startOffset = Math.max(0, (s - min) / 86400000)
  const duration = Math.max(1, (e - s) / 86400000 + 1)
  const left = (startOffset / days * 100).toFixed(2) + '%'
  const width = (duration / days * 100).toFixed(2) + '%'
  return { left, width, position: 'absolute', top: '4px', height: '20px' }
}

// ====== 数据加载 ======
const loadProjects = async () => {
  try { const res = await getAllProjects(); projects.value = res.data || [] } catch { /* ignore */ }
}

const loadAllMilestones = async () => {
  try {
    const params = {}
    if (filterProjectId.value) params.projectId = filterProjectId.value
    const res = await getAllMilestones(params)
    allMilestones.value = res.data || []
  } catch { /* ignore */ }
}

const fetchMilestones = async () => {
  loading.value = true
  try {
    const params = { page: milestonePage.value, size: 20 }
    if (filterProjectId.value) params.projectId = filterProjectId.value
    const res = await getMilestoneList(params)
    milestoneData.value = res.data.records || []
    milestoneTotal.value = res.data.total || 0
  } finally { loading.value = false }
}

const fetchGanttTasks = async () => {
  loading.value = true
  try {
    const params = { page: ganttPage.value, size: 200 }
    if (filterProjectId.value) params.projectId = filterProjectId.value
    const res = await getGanttTaskList(params)
    const allRecords = res.data.records || []
    // 构建树形排列：先里程碑，每个里程碑后跟其子任务
    const milestones = allRecords.filter(t => t.task_type === 1).sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    const tasks = allRecords.filter(t => t.task_type === 2)
    const sorted = []
    milestones.forEach(m => {
      sorted.push(m)
      tasks.filter(t => t.parent_id === m.id).sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0)).forEach(t => sorted.push(t))
    })
    // 无里程碑的独立任务
    tasks.filter(t => !t.parent_id || !milestones.find(m => m.id === t.parent_id)).forEach(t => sorted.push(t))
    ganttData.value = sorted
    ganttTotal.value = res.data.total || 0
  } finally { loading.value = false }
}

const fetchChanges = async () => {
  changeLoading.value = true
  try {
    const params = { page: changePage.value, size: 20 }
    if (filterProjectId.value) params.projectId = filterProjectId.value
    const res = await getChangeOrderList(params)
    changeData.value = res.data.records || []
    changeTotal.value = res.data.total || 0
  } finally { changeLoading.value = false }
}

const handleProjectChange = () => {
  milestonePage.value = 1; ganttPage.value = 1; changePage.value = 1
  fetchCurrentTab()
}

const handleTabChange = () => { fetchCurrentTab() }

const fetchCurrentTab = () => {
  if (activeTab.value === 'milestone') fetchMilestones()
  else if (activeTab.value === 'gantt') fetchGanttTasks()
  else fetchChanges()
}

// ====== 里程碑 CRUD ======
const handleAddMilestone = () => {
  isEditMs.value = false; editMsId.value = null
  msDialogTitle.value = '新建里程碑'
  if (filterProjectId.value) msForm.projectId = filterProjectId.value
  loadProjects(); loadAllMilestones()
  msDialogVisible.value = true
}

const handleEditMilestone = (row) => {
  isEditMs.value = true; editMsId.value = row.id
  msDialogTitle.value = '编辑里程碑'
  Object.assign(msForm, {
    projectId: row.project_id,
    milestoneName: row.task_name,
    deadline: row.plan_end_date,
    actualEndDate: row.actual_end_date || null,
    sortOrder: row.sort_order || 0,
    depMilestoneIds: row.dep_milestone_ids || []
  })
  loadProjects(); loadAllMilestones()
  msDialogVisible.value = true
}

const resetMsForm = () => {
  Object.assign(msForm, { projectId: null, milestoneName: '', deadline: null, actualEndDate: null, sortOrder: 0, depMilestoneIds: [] })
  msFormRef.value?.resetFields()
}

const submitMilestone = async () => {
  await msFormRef.value.validate()
  submitting.value = true
  try {
    if (isEditMs.value) {
      await updateMilestone(editMsId.value, msForm)
      ElMessage.success('更新成功')
    } else {
      await createMilestone(msForm)
      ElMessage.success('创建成功')
    }
    msDialogVisible.value = false
    fetchMilestones(); loadAllMilestones()
  } finally { submitting.value = false }
}

const handleMilestoneStatusChange = async (row, status) => {
  await ElMessageBox.confirm(`确定将状态改为"${statusLabel[status]}"？`, '提示', { type: 'warning' })
  await updateGanttTaskStatus(row.id, status)
  ElMessage.success('状态已更新')
  fetchMilestones()
}

const handleDeleteMilestone = async (row) => {
  await ElMessageBox.confirm('确定删除该里程碑？', '提示', { type: 'warning' })
  await deleteMilestone(row.id)
  ElMessage.success('删除成功')
  fetchMilestones(); loadAllMilestones()
}

// ====== 甘特任务 CRUD ======
const handleAddGantt = () => {
  isEditGantt.value = false; editGanttId.value = null
  ganttForm.taskType = 2; ganttForm.parentId = null
  ganttDialogTitle.value = '新建任务'
  loadProjects(); loadAllMilestones()
  ganttDialogVisible.value = true
}

const handleEditGanttRow = (row) => {
  isEditGantt.value = true; editGanttId.value = row.id
  ganttDialogTitle.value = row.task_type === 1 ? '编辑里程碑' : '编辑任务'
  populateGanttForm(row)
  loadProjects(); loadAllMilestones()
  ganttDialogVisible.value = true
}

const populateGanttForm = (row) => {
  Object.assign(ganttForm, {
    projectId: row.project_id, parentId: row.parent_id || null, taskName: row.task_name || '',
    taskType: row.task_type, planStartDate: row.plan_start_date || null, planEndDate: row.plan_end_date || null,
    actualStartDate: row.actual_start_date || null, actualEndDate: row.actual_end_date || null,
    progressPct: row.progress_pct || 0, sortOrder: row.sort_order || 0,
    dependencyType: row.dependency_type || null, dependencyTaskId: row.dependency_task_id || null
  })
}

const resetGanttForm = () => {
  Object.assign(ganttForm, {
    projectId: null, parentId: null, taskName: '', taskType: 1,
    planStartDate: null, planEndDate: null, actualStartDate: null, actualEndDate: null,
    progressPct: 0, sortOrder: 0, dependencyType: null, dependencyTaskId: null
  })
  ganttFormRef.value?.resetFields()
}

const submitGantt = async () => {
  await ganttFormRef.value.validate()
  submitting.value = true
  try {
    if (isEditGantt.value) {
      await updateGanttTask(editGanttId.value, ganttForm)
      ElMessage.success('更新成功')
    } else {
      await createGanttTask(ganttForm)
      ElMessage.success('创建成功')
    }
    ganttDialogVisible.value = false
    fetchCurrentTab(); loadAllMilestones()
  } finally { submitting.value = false }
}

const handleStatusChange = async (row, status) => {
  await ElMessageBox.confirm(`确定将状态改为"${statusLabel[status]}"？`, '提示', { type: 'warning' })
  await updateGanttTaskStatus(row.id, status)
  ElMessage.success('状态已更新')
  fetchCurrentTab()
}

const handleDeleteGantt = async (row) => {
  const label = row.task_type === 1 ? '里程碑' : '任务'
  await ElMessageBox.confirm(`确定删除该${label}？`, '提示', { type: 'warning' })
  await deleteGanttTask(row.id)
  ElMessage.success('删除成功')
  fetchCurrentTab(); loadAllMilestones()
}

// ====== 变更单 CRUD ======
const handleAddChange = () => {
  isEditChange.value = false; editChangeId.value = null
  loadProjects()
  changeDialogVisible.value = true
}

const handleEditChange = async (row) => {
  isEditChange.value = true; editChangeId.value = row.id
  loadProjects()
  let existingDetails = [createEmptyDetail()]
  try {
    const res = await getChangeOrderDetails(row.id)
    const details = res.data || []
    if (details.length > 0) {
      existingDetails = details.map(d => ({
        itemName: d.item_name || '', specModel: d.spec_model || '', unit: d.unit || '',
        planQuantity: d.plan_quantity, actualQuantity: d.actual_quantity, unitPrice: d.unit_price
      }))
    }
  } catch { /* ignore */ }
  Object.assign(changeForm, {
    projectId: row.project_id, contractId: row.contract_id || null, changeType: row.change_type || '', title: row.title || '',
    description: row.description || '', details: existingDetails
  })
  changeDialogVisible.value = true
}

const resetChangeForm = () => {
  Object.assign(changeForm, {
    projectId: null, contractId: null, changeType: '', title: '', description: '',
    details: [createEmptyDetail()]
  })
  changeFormRef.value?.resetFields()
}

const addChangeDetail = () => { changeForm.details.push(createEmptyDetail()) }

const submitChange = async () => {
  await changeFormRef.value.validate()
  const details = changeForm.details.filter(d => d.itemName).map(d => {
    const diff = (d.actualQuantity || 0) - (d.planQuantity || 0)
    return { ...d, diffQuantity: diff, subtotal: diff * (d.unitPrice || 0) }
  })
  const totalAmount = details.reduce((s, d) => s + (d.subtotal || 0), 0)
  const payload = { projectId: changeForm.projectId, contractId: changeForm.contractId, changeType: changeForm.changeType, title: changeForm.title, description: changeForm.description, totalAmount, details }
  submitting.value = true
  try {
    if (isEditChange.value) { await updateChangeOrder(editChangeId.value, payload); ElMessage.success('更新成功') }
    else { await createChangeOrder(payload); ElMessage.success('创建成功') }
    changeDialogVisible.value = false; fetchChanges()
  } finally { submitting.value = false }
}

const handleChangeStatusUpdate = async (row, status) => {
  await ElMessageBox.confirm(`确定将状态改为"${changeStatusLabel[status]}"？`, '提示', { type: 'warning' })
  await updateChangeOrderStatus(row.id, status)
  ElMessage.success('状态已更新')
  fetchChanges()
}

const handleDeleteChange = async (row) => {
  await ElMessageBox.confirm('确定删除该变更单？', '提示', { type: 'warning' })
  await deleteChangeOrder(row.id)
  ElMessage.success('删除成功')
  fetchChanges()
}

onMounted(() => {
  loadProjects()
  loadAllMilestones()
  fetchMilestones()
})
</script>

<style scoped>
.milestone-timeline-wrapper {
  overflow-x: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-extra-light);
  padding: 12px;
}
.milestone-timeline {
  position: relative;
  min-height: 160px;
}
.milestone-dep-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}
.milestone-node {
  position: absolute;
  width: 120px;
  text-align: center;
}
.milestone-diamond {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}
.milestone-icon {
  font-size: 28px;
  line-height: 1;
}
.ms-draft .milestone-icon { color: #909399; }
.ms-pending .milestone-icon { color: #e6a23c; }
.ms-approved .milestone-icon { color: #67c23a; }
.ms-locked .milestone-icon { color: #303133; }
.milestone-label {
  font-size: 12px;
  font-weight: 600;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.milestone-date {
  font-size: 11px;
  color: #909399;
  margin-top: 1px;
}
.gantt-chart-wrapper {
  overflow-x: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
.gantt-chart {
  width: 100%;
  border-collapse: collapse;
}
.gantt-chart th {
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color);
  font-size: 13px;
  font-weight: 500;
}
.gantt-chart td {
  border-bottom: 1px solid var(--el-border-color-extra-light);
  font-size: 13px;
}
.gantt-milestone-row {
  background: var(--el-color-primary-light-9);
}
.gantt-bar-container {
  position: relative;
  height: 28px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.gantt-bar {
  background: var(--el-color-primary-light-5);
  border-radius: 3px;
  overflow: hidden;
  cursor: pointer;
}
.gantt-bar-fill {
  height: 100%;
  background: var(--el-color-primary);
  border-radius: 3px;
  transition: width 0.3s;
}
</style>
