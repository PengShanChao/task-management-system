<template>
  <div class="news-page">
    <div class="page-header">
      <h2>Tech News</h2>
      <div class="header-right">
        <span v-if="lastRefreshed" class="last-refreshed">Last refreshed: {{ lastRefreshed }}</span>
        <el-button type="success" :loading="refreshing" @click="handleRefresh">Refresh</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="Source">
          <el-select v-model="sourceFilter" clearable placeholder="All" @change="fetchNews">
            <el-option v-for="s in sources" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="Keyword">
          <el-input v-model="keyword" placeholder="Search title or summary..." clearable
            style="width: 300px" @keyup.enter="fetchNews" @clear="fetchNews" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchNews">Search</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="newsList" v-loading="loading" stripe>
      <el-table-column prop="title" label="Title" min-width="300">
        <template #default="{ row }">
          <div class="title-cell">
            <el-link :href="row.url" target="_blank" type="primary">{{ row.title }}</el-link>
            <el-tag v-if="row.relatedToCurrentUserTasks" size="small" type="warning" class="related-tag">
              Related
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="Source" width="200" />
      <el-table-column prop="publishedAt" label="Published" width="180">
        <template #default="{ row }">
          {{ row.publishedAt ? new Date(row.publishedAt).toLocaleString() : '-' }}
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next"
      @change="fetchNews"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { newsApi } from '@/api/newsApi'
import { ElMessage } from 'element-plus'

const newsList = ref([])
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const keyword = ref('')
const sourceFilter = ref('')
const page = ref(1)
const pageSize = ref(10)
const sources = ref([])
const lastRefreshed = ref('')

onMounted(() => fetchNews())

async function fetchNews() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (sourceFilter.value) params.source = sourceFilter.value
    const res = await newsApi.list(params)
    newsList.value = res.data.records || []
    total.value = res.data.total || 0
    // Collect unique sources for filter dropdown
    const srcSet = new Set(sources.value)
    newsList.value.forEach(n => { if (n.source) srcSet.add(n.source) })
    if (res.data.total > 0 && newsList.value.length > 0 && newsList.value[0].createdAt) {
      lastRefreshed.value = new Date(newsList.value[0].createdAt).toLocaleString()
    }
  } finally {
    loading.value = false
  }
}

async function handleRefresh() {
  refreshing.value = true
  try {
    const res = await newsApi.refresh()
    const count = res.data
    if (count > 0) {
      ElMessage.success(`Fetched ${count} new items`)
    } else {
      ElMessage.info('News is up to date')
    }
    page.value = 1
    keyword.value = ''
    sourceFilter.value = ''
    await fetchNews()
  } catch {
    // handled by interceptor
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped>
.news-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; color: #303133; }
.header-right { display: flex; align-items: center; gap: 12px; }
.last-refreshed { font-size: 13px; color: #909399; }
.filter-card { margin-bottom: 16px; }
.title-cell { display: flex; align-items: center; gap: 8px; }
.related-tag { flex-shrink: 0; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
