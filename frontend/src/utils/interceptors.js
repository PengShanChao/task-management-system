import axios from 'axios'
import { ElMessage } from 'element-plus'

axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axios.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || 'Request failed')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        sessionStorage.setItem('loginError', error.response.data?.message || 'Session expired, please login again')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('Access denied')
      } else if (status === 400) {
        ElMessage.error(error.response.data?.message || 'Bad request')
      } else {
        ElMessage.error('Server error')
      }
    } else {
      ElMessage.error('Network error')
    }
    return Promise.reject(error)
  }
)
