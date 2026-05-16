<template>
  <div class="task-list-page">
    <div class="page-header">
      <h2>My Tasks</h2>
      <div class="header-actions">
        <el-button-group class="view-toggle">
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'">Table</el-button>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'">Card</el-button>
        </el-button-group>
        <el-button type="primary" @click="showCreateDialog">Create Task</el-button>
        <el-dropdown @command="handleExport">
          <el-button>Export <el-icon><ArrowDown /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="csv">CSV</el-dropdown-item>
              <el-dropdown-item command="xlsx">Excel</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <el-row :gutter="16" class="stats-bar">
      <el-col :span="4">
        <div class="stat-item stat-todo">
          <span class="stat-num">{{ stats.todo }}</span>
          <span class="stat-label">Todo</span>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item stat-progress">
          <span class="stat-num">{{ stats.inProgress }}</span>
          <span class="stat-label">In Progress</span>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item stat-done">
          <span class="stat-num">{{ stats.done }}</span>
          <span class="stat-label">Done</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item stat-rate">
          <span class="stat-num">{{ stats.completionRate }}%</span>
          <span class="stat-label">Completion Rate</span>
        </div>
      </el-col>
      <el-col :span="6">
        <v-chart :option="rateChartOption" style="height: 60px" />
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="Status">
          <el-select v-model="filters.status" clearable placeholder="All" @change="loadTasks">
            <el-option label="Todo" value="TODO" />
            <el-option label="In Progress" value="IN_PROGRESS" />
            <el-option label="Done" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="filters.priority" clearable placeholder="All" @change="loadTasks">
            <el-option label="High" value="HIGH" />
            <el-option label="Medium" value="MEDIUM" />
            <el-option label="Low" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="authStore.role === 'ADMIN'" label="Assignee">
          <el-select v-model="filters.userId" clearable placeholder="All" @change="loadTasks">
            <el-option v-for="u in users" :key="u.id" :label="u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Deadline From">
          <el-date-picker v-model="filters.deadlineFrom" type="datetime" placeholder="Select datetime"
            value-format="YYYY-MM-DD HH:mm" clearable @change="loadTasks" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">Search</el-button>
          <el-button @click="resetFilters">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table View -->
    <el-table v-if="viewMode === 'table'" :data="taskStore.tasks" v-loading="taskStore.loading" stripe class="task-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="Title" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="showDetail(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="Priority" width="100">
        <template #default="{ row }">
          <el-tag :type="priorityTagType(row.priority)" size="small">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Deadline" width="160">
        <template #default="{ row }">
          <span :style="{ color: countdownColor(row.deadline) }">{{ countdownText(row.deadline) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="Assignee" width="100" />
      <el-table-column label="Actions" width="320" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'TODO'" size="small" type="primary"
            @click="changeStatus(row, 'IN_PROGRESS')">Start</el-button>
          <el-button v-if="row.status === 'IN_PROGRESS'" size="small" type="success"
            @click="changeStatus(row, 'DONE')">Complete</el-button>
          <el-button size="small" @click="editTask(row)">Edit</el-button>
          <el-popconfirm title="Delete this task?" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">Delete</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- Card View -->
    <el-row v-else :gutter="16" class="card-grid">
      <el-col v-for="task in taskStore.tasks" :key="task.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="task-card" :class="'card-' + task.status.toLowerCase()">
          <template #header>
            <div class="card-header">
              <el-link type="primary" @click="showDetail(task)">{{ task.title }}</el-link>
              <el-tag :type="statusTagType(task.status)" size="small">{{ statusLabel(task.status) }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">Priority</span>
              <el-tag :type="priorityTagType(task.priority)" size="small">{{ task.priority }}</el-tag>
            </div>
            <div class="card-row">
              <span class="card-label">Deadline</span>
              <span :style="{ color: countdownColor(task.deadline) }" class="card-countdown">{{ countdownText(task.deadline) }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">Assignee</span>
              <span>{{ task.username }}</span>
            </div>
          </div>
          <div class="card-actions">
            <el-button v-if="task.status === 'TODO'" size="small" type="primary"
              @click="changeStatus(task, 'IN_PROGRESS')">Start</el-button>
            <el-button v-if="task.status === 'IN_PROGRESS'" size="small" type="success"
              @click="changeStatus(task, 'DONE')">Complete</el-button>
            <el-button size="small" @click="editTask(task)">Edit</el-button>
            <el-popconfirm title="Delete this task?" @confirm="handleDelete(task.id)">
              <template #reference>
                <el-button size="small" type="danger">Delete</el-button>
              </template>
            </el-popconfirm>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!taskStore.loading && taskStore.tasks.length === 0" class="empty-state">
      No tasks found
    </div>

    <el-pagination
      class="pagination"
      v-model:current-page="filters.page"
      v-model:page-size="filters.pageSize"
      :total="taskStore.total"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next"
      @change="loadTasks"
    />

    <TaskDialog ref="taskDialogRef" @success="loadTasks" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTaskStore } from '@/stores/taskStore'
import { useAuthStore } from '@/stores/authStore'
import { userApi } from '@/api/userApi'
import { dashboardApi } from '@/api/dashboardApi'
import TaskDialog from '@/components/TaskDialog.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ArrowDown } from '@element-plus/icons-vue'

use([PieChart, TooltipComponent, CanvasRenderer])

const taskStore = useTaskStore()
const authStore = useAuthStore()
const taskDialogRef = ref(null)
const users = ref([])
const viewMode = ref('table')
const now = ref(Date.now())
let timer = null

const filters = reactive({
  status: '', priority: '', deadlineFrom: '', userId: '', page: 1, pageSize: 10
})

const stats = reactive({
  todo: 0, inProgress: 0, done: 0, completionRate: 0
})

async function loadStats() {
  try {
    const res = await dashboardApi.getStats()
    const d = res.data
    stats.todo = d.todoCount
    stats.inProgress = d.inProgressCount
    stats.done = d.doneCount
    stats.completionRate = d.completionRate
  } catch { /* ignore */ }
}

const rateChartOption = computed(() => {
  const total = stats.todo + stats.inProgress + stats.done
  const allTodo = total > 0 && stats.todo === total
  return {
    tooltip: { show: false },
    series: [{
      type: 'pie',
      radius: ['65%', '85%'],
      center: ['50%', '50%'],
      label: { show: false },
      data: [
        { value: allTodo ? 1 : stats.todo, name: 'Todo', itemStyle: { color: '#909399' } },
        { value: stats.inProgress, name: 'In Progress', itemStyle: { color: '#e6a23c' } },
        { value: stats.done, name: 'Done', itemStyle: { color: '#67c23a' } }
      ],
      silent: true
    }]
  }
})

onMounted(() => {
  loadTasks()
  timer = setInterval(() => { now.value = Date.now() }, 60000)
  if (authStore.role === 'ADMIN') {
    userApi.list().then(res => { users.value = res.data })
  }
})

onUnmounted(() => {
  clearInterval(timer)
})

function loadTasks() {
  const params = {}
  if (filters.status) params.status = filters.status
  if (filters.priority) params.priority = filters.priority
  if (filters.deadlineFrom) params.deadlineFrom = filters.deadlineFrom
  if (filters.userId) params.userId = filters.userId
  params.page = filters.page
  params.pageSize = filters.pageSize
  taskStore.fetchTasks(params)
  loadStats()
}

function resetFilters() {
  filters.status = ''
  filters.priority = ''
  filters.deadlineFrom = ''
  filters.userId = ''
  filters.page = 1
  loadTasks()
}

function showCreateDialog() { taskDialogRef.value.open() }
function showDetail(row) { taskDialogRef.value.open(row) }
function editTask(row) { taskDialogRef.value.open(row) }

async function changeStatus(row, newStatus) {
  await taskStore.updateTask(row.id, { title: row.title, status: newStatus })
  loadStats()
}

async function handleDelete(id) {
  await taskStore.deleteTask(id)
  loadTasks()
  loadStats()
}

function countdownText(deadline) {
  if (!deadline) return '-'
  const current = new Date(now.value)
  const dl = new Date(deadline)
  const diffMs = dl - current
  const absMs = Math.abs(diffMs)
  const totalHours = Math.floor(absMs / (1000 * 60 * 60))
  const days = Math.floor(totalHours / 24)
  const hours = totalHours % 24
  const minutes = Math.floor((absMs % (1000 * 60 * 60)) / (1000 * 60))

  if (diffMs < 0) {
    if (days > 0) return `${days}d ${hours}h overdue`
    if (hours > 0) return `${hours}h ${minutes}m overdue`
    return `${minutes}m overdue`
  }
  if (days > 0) return `${days}d ${hours}h`
  if (hours > 0) return `${hours}h ${minutes}m`
  return `${minutes}m`
}

function countdownColor(deadline) {
  if (!deadline) return '#909399'
  const diffMs = new Date(deadline) - new Date(now.value)
  const diffHours = diffMs / (1000 * 60 * 60)
  if (diffHours <= 0) return '#f56c6c'
  if (diffHours <= 24) return '#e6a23c'
  return '#67c23a'
}

async function handleExport(format) {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`/api/export/tasks?format=${format}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `tasks.${format}`
    a.click()
    URL.revokeObjectURL(url)
  } catch { /* ignore */ }
}

function statusTagType(s) { return { TODO: 'info', IN_PROGRESS: 'warning', DONE: 'success' }[s] || 'info' }
function statusLabel(s) { return { TODO: 'Todo', IN_PROGRESS: 'In Progress', DONE: 'Done' }[s] || s }
function priorityTagType(p) { return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }[p] || 'info' }
</script>

<style scoped>
.task-list-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; color: #303133; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.view-toggle { margin-right: 4px; }
.filter-card { margin-bottom: 16px; }

.stats-bar { margin-bottom: 16px; }
.stat-item { text-align: center; padding: 12px 8px; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-num { font-size: 24px; font-weight: 700; display: block; }
.stat-label { font-size: 12px; color: #909399; }
.stat-todo .stat-num { color: #909399; }
.stat-progress .stat-num { color: #e6a23c; }
.stat-done .stat-num { color: #67c23a; }
.stat-rate .stat-num { color: #409eff; }

.task-table { width: 100%; }

.card-grid { margin-bottom: 16px; }
.task-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-body { margin-bottom: 12px; }
.card-row { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; }
.card-label { font-size: 13px; color: #909399; }
.card-countdown { font-size: 13px; font-weight: 500; }
.card-actions { display: flex; gap: 6px; flex-wrap: wrap; }
.card-todo { border-left: 3px solid #909399; }
.card-in_progress { border-left: 3px solid #e6a23c; }
.card-done { border-left: 3px solid #67c23a; }

.empty-state { text-align: center; padding: 40px; color: #c0c4cc; font-size: 16px; }

.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
