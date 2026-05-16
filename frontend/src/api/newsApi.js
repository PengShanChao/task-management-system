import axios from 'axios'

export const newsApi = {
  list(params) {
    return axios.get('/api/news', { params })
  },
  search(keyword, page = 1, pageSize = 10) {
    return axios.get('/api/news/search', { params: { keyword, page, pageSize } })
  },
  refresh() {
    return axios.post('/api/news/refresh')
  },
  related() {
    return axios.get('/api/news/related')
  },
  getByTaskId(taskId) {
    return axios.get(`/api/tasks/${taskId}/news`)
  },
  linkTask(taskId, newsId) {
    return axios.post(`/api/tasks/${taskId}/news/${newsId}`)
  },
  unlinkTask(taskId, newsId) {
    return axios.delete(`/api/tasks/${taskId}/news/${newsId}`)
  }
}
