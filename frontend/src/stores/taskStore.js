import { defineStore } from 'pinia'
import { ref } from 'vue'
import { taskApi } from '@/api/taskApi'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchTasks(params = {}) {
    loading.value = true
    try {
      const res = await taskApi.list(params)
      tasks.value = res.data.records || []
      total.value = res.data.total || 0
    } finally {
      loading.value = false
    }
  }

  async function createTask(data) {
    await taskApi.create(data)
    await fetchTasks()
  }

  async function updateTask(id, data) {
    await taskApi.update(id, data)
    await fetchTasks()
  }

  async function deleteTask(id) {
    await taskApi.delete(id)
    await fetchTasks()
  }

  return { tasks, total, loading, fetchTasks, createTask, updateTask, deleteTask }
})
