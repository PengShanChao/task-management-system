import axios from 'axios'

export const userApi = {
  list() {
    return axios.get('/api/users')
  }
}
