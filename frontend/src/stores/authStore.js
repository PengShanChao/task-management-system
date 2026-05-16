import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const role = computed(() => user.value?.role || 'USER')

  async function login(credentials) {
    const res = await authApi.login(credentials)
    const data = res.data
    token.value = data.token
    user.value = { id: data.userId, username: data.username, role: data.role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    return data
  }

  async function register(credentials) {
    const res = await authApi.register(credentials)
    const data = res.data
    token.value = data.token
    user.value = { id: data.userId, username: data.username, role: data.role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    return data
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, username, role, login, register, logout }
})
