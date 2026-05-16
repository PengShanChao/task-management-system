import axios from 'axios'

export const authApi = {
  login(data) {
    return axios.post('/api/auth/login', data)
  },
  register(data) {
    return axios.post('/api/auth/register', data)
  }
}
