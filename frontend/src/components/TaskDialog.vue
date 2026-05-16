<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? 'Edit Task' : isView ? 'Task Detail' : 'Create Task'"
    width="680px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="isView">
      <el-form-item label="Title" prop="title">
        <el-input v-model="form.title" placeholder="Task title" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Task description" />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="Todo" value="TODO" />
          <el-option label="In Progress" value="IN_PROGRESS" />
          <el-option label="Done" value="DONE" />
        </el-select>
      </el-form-item>
      <el-form-item label="Priority" prop="priority">
        <el-select v-model="form.priority" style="width: 100%">
          <el-option label="Low" value="LOW" />
          <el-option label="Medium" value="MEDIUM" />
          <el-option label="High" value="HIGH" />
        </el-select>
      </el-form-item>
      <el-form-item label="Deadline">
        <el-date-picker v-model="form.deadline" type="datetime" placeholder="Select deadline"
          format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
      </el-form-item>
    </el-form>

    <template v-if="editingId">
      <el-divider>Related News</el-divider>
      <div class="news-section">
        <div v-if="linkedNews.length > 0" class="news-group">
          <p class="news-group-title">Linked News</p>
          <div v-for="n in linkedNews" :key="n.id" class="news-item">
            <el-link :href="n.url" target="_blank" type="primary">{{ n.title }}</el-link>
            <span class="news-source">{{ n.source }}</span>
            <el-button size="small" type="danger" circle :icon="CloseBold" @click="handleUnlink(n.id)" />
          </div>
        </div>
        <div v-if="suggestedNews.length > 0" class="news-group">
          <p class="news-group-title">Suggested News</p>
          <div v-for="n in suggestedNews" :key="n.id" class="news-item">
            <el-link :href="n.url" target="_blank" type="primary">{{ n.title }}</el-link>
            <span class="news-source">{{ n.source }}</span>
            <el-button size="small" type="primary" circle @click="handleLink(n.id)">+</el-button>
          </div>
        </div>
        <div v-if="linkedNews.length === 0 && suggestedNews.length === 0" class="news-empty">
          No related news found
        </div>
        <div class="news-search">
          <el-input v-model="newsKeyword" placeholder="Search news to link..." size="small"
            style="width: 300px; margin-right: 8px" @keyup.enter="searchNewsToLink" />
          <el-button size="small" @click="searchNewsToLink">Search</el-button>
        </div>
        <div v-if="searchResults.length > 0" class="news-group">
          <p class="news-group-title">Search Results</p>
          <div v-for="n in searchResults" :key="n.id" class="news-item">
            <el-link :href="n.url" target="_blank" type="primary">{{ n.title }}</el-link>
            <span class="news-source">{{ n.source }}</span>
            <el-button size="small" type="primary" circle @click="handleLinkFromSearch(n.id)">+</el-button>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <el-button @click="visible = false">{{ isView ? 'Close' : 'Cancel' }}</el-button>
      <el-button v-if="!isView" type="primary" @click="handleSubmit">
        {{ isEdit ? 'Update' : 'Create' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { taskApi } from '@/api/taskApi'
import { newsApi } from '@/api/newsApi'
import { ElMessage } from 'element-plus'
import { CloseBold } from '@element-plus/icons-vue'

const emit = defineEmits(['success'])
const visible = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const viewOnly = ref(false)

const isEdit = computed(() => !!editingId.value)
const isView = computed(() => viewOnly.value)

const form = reactive({
  title: '', description: '', status: 'TODO', priority: 'MEDIUM', deadline: null
})

const rules = {
  title: [{ required: true, message: 'Title is required', trigger: 'blur' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }],
  priority: [{ required: true, message: 'Priority is required', trigger: 'change' }]
}

const linkedNews = ref([])
const suggestedNews = ref([])
const newsKeyword = ref('')
const searchResults = ref([])

function open(task) {
  viewOnly.value = false
  linkedNews.value = []
  suggestedNews.value = []
  newsKeyword.value = ''
  searchResults.value = []

  if (task) {
    editingId.value = task.id
    form.title = task.title || ''
    form.description = task.description || ''
    form.status = task.status || 'TODO'
    form.priority = task.priority || 'MEDIUM'
    if (task.deadline) {
      const d = new Date(task.deadline)
      const pad = (n) => String(n).padStart(2, '0')
      form.deadline = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    } else {
      form.deadline = null
    }
    if (!task.title) {
      viewOnly.value = true
    }
    loadRelatedNews(task.id)
  } else {
    editingId.value = null
    form.title = ''
    form.description = ''
    form.status = 'TODO'
    form.priority = 'MEDIUM'
    form.deadline = null
  }
  visible.value = true
}

async function loadRelatedNews(taskId) {
  try {
    const res = await newsApi.getByTaskId(taskId)
    const all = res.data || []
    linkedNews.value = all.filter(n => n.relatedToCurrentUserTasks)
    suggestedNews.value = all.filter(n => !n.relatedToCurrentUserTasks)
  } catch {
    // ignore errors loading news
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const data = { ...form, deadline: form.deadline || undefined }
  try {
    if (isEdit.value) {
      await taskApi.update(editingId.value, data)
      ElMessage.success('Task updated')
    } else {
      await taskApi.create(data)
      ElMessage.success('Task created')
    }
    visible.value = false
    emit('success')
  } catch (e) {
    // handled by interceptor
  }
}

async function handleLink(newsId) {
  try {
    await newsApi.linkTask(editingId.value, newsId)
    ElMessage.success('News linked')
    loadRelatedNews(editingId.value)
  } catch {
    // handled by interceptor
  }
}

async function handleUnlink(newsId) {
  try {
    await newsApi.unlinkTask(editingId.value, newsId)
    ElMessage.success('News unlinked')
    loadRelatedNews(editingId.value)
  } catch {
    // handled by interceptor
  }
}

async function searchNewsToLink() {
  if (!newsKeyword.value) return
  try {
    const res = await newsApi.search(newsKeyword.value, 1, 10)
    const linkedIds = linkedNews.value.map(n => n.id)
    searchResults.value = (res.data.records || []).filter(n => !linkedIds.includes(n.id))
  } catch {
    // ignore
  }
}

async function handleLinkFromSearch(newsId) {
  try {
    await newsApi.linkTask(editingId.value, newsId)
    ElMessage.success('News linked')
    searchResults.value = []
    newsKeyword.value = ''
    loadRelatedNews(editingId.value)
  } catch {
    // handled by interceptor
  }
}

defineExpose({ open })
</script>

<style scoped>
.news-section {
  max-height: 300px;
  overflow-y: auto;
}
.news-group {
  margin-bottom: 12px;
}
.news-group-title {
  font-size: 13px;
  color: #909399;
  margin: 0 0 8px 0;
}
.news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;
}
.news-source {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
  margin-left: auto;
  margin-right: 8px;
}
.news-empty {
  color: #c0c4cc;
  font-size: 13px;
  text-align: center;
  padding: 16px 0;
}
.news-search {
  display: flex;
  align-items: center;
  margin-top: 12px;
}
</style>
