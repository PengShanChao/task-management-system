import axios from 'axios'

export const dashboardApi = {
  getStats() {
    return axios.get('/api/dashboard/stats')
  }
}
