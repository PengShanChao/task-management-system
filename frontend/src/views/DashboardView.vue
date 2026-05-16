<template>
  <div class="dashboard-page">
    <h2>Dashboard</h2>
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <p class="stat-value">{{ stats.totalTasks }}</p>
          <p class="stat-label">Total Tasks</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-todo">
          <p class="stat-value">{{ stats.todoCount }}</p>
          <p class="stat-label">Todo</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-progress">
          <p class="stat-value">{{ stats.inProgressCount }}</p>
          <p class="stat-label">In Progress</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-done">
          <p class="stat-value">{{ stats.doneCount }}</p>
          <p class="stat-label">Completed</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-high">
          <p class="stat-value">{{ stats.highPriorityCount }}</p>
          <p class="stat-label">High Priority</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-overdue">
          <p class="stat-value">{{ stats.overdueCount }}</p>
          <p class="stat-label">Overdue</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-rate">
          <p class="stat-value">{{ stats.completionRate }}%</p>
          <p class="stat-label">Completion Rate</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>Task Completion Chart</template>
      <v-chart :option="chartOption" style="height: 350px" />
    </el-card>

    <el-card class="news-card">
      <template #header>
        <div class="news-card-header">
          <span>Related News</span>
          <el-link type="primary" :underline="false" @click="$router.push('/news')">View All</el-link>
        </div>
      </template>
      <div v-if="latestNews.length === 0" class="news-empty">No related news yet</div>
      <div v-for="n in latestNews" :key="n.id" class="news-row">
        <el-link :href="n.url" target="_blank" type="primary">{{ n.title }}</el-link>
        <span class="news-source">{{ n.source }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { dashboardApi } from '@/api/dashboardApi'
import { newsApi } from '@/api/newsApi'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const stats = reactive({
  totalTasks: 0, todoCount: 0, inProgressCount: 0, doneCount: 0,
  highPriorityCount: 0, overdueCount: 0, completionRate: 0
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%' },
  series: [{
    name: 'Tasks',
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    label: { show: true, formatter: '{b}: {c}' },
    data: [
      { value: stats.todoCount, name: 'Todo', itemStyle: { color: '#909399' } },
      { value: stats.inProgressCount, name: 'In Progress', itemStyle: { color: '#e6a23c' } },
      { value: stats.doneCount, name: 'Done', itemStyle: { color: '#67c23a' } }
    ]
  }]
}))

const latestNews = ref([])

onMounted(async () => {
  try {
    const res = await dashboardApi.getStats()
    Object.assign(stats, res.data)
  } catch (e) {
    // handled by interceptor
  }
  try {
    const res = await newsApi.related()
    latestNews.value = res.data || []
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.dashboard-page { max-width: 1200px; }
.dashboard-page h2 { color: #303133; margin-bottom: 16px; }
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; margin: 0 0 8px 0; }
.stat-label { font-size: 14px; color: #909399; margin: 0; }
.stat-total .stat-value { color: #409eff; }
.stat-todo .stat-value { color: #909399; }
.stat-progress .stat-value { color: #e6a23c; }
.stat-done .stat-value { color: #67c23a; }
.stat-high .stat-value { color: #f56c6c; }
.stat-overdue .stat-value { color: #e6a23c; }
.stat-rate .stat-value { color: #409eff; }
.chart-card { margin-top: 16px; }
.news-card { margin-top: 16px; }
.news-card-header { display: flex; justify-content: space-between; align-items: center; }
.news-row { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.news-row:last-child { border-bottom: none; }
.news-source { font-size: 12px; color: #c0c4cc; margin-left: auto; flex-shrink: 0; }
.news-empty { color: #c0c4cc; font-size: 13px; text-align: center; padding: 16px 0; }
</style>
