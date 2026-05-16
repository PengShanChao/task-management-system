<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">Task Management System</h1>
      <p class="login-subtitle">Login or register to manage your tasks</p>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="Login" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
            <el-form-item label="Username" prop="username">
              <el-input v-model="loginForm.username" placeholder="Enter username" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="Enter password"
                show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" :loading="loading" block @click="handleLogin">
              Login
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Register" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
            <el-form-item label="Username" prop="username">
              <el-input v-model="registerForm.username" placeholder="3-50 characters" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="At least 6 characters"
                show-password />
            </el-form-item>
            <el-form-item label="Confirm Password" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="Re-enter password"
                show-password />
            </el-form-item>
            <el-button type="success" :loading="loading" block @click="handleRegister">
              Register
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/authApi'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })

const loginRules = {
  username: [{ required: true, message: 'Username is required', trigger: 'blur' }],
  password: [{ required: true, message: 'Password is required', trigger: 'blur' }]
}

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('Please confirm your password'))
  } else if (value !== registerForm.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: 'Username is required', trigger: 'blur' },
    { min: 3, max: 50, message: '3-50 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Password is required', trigger: 'blur' },
    { min: 6, message: 'At least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loginFormRef = ref(null)
const registerFormRef = ref(null)

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(loginForm)
    ElMessage.success('Login successful')
    router.push('/tasks')
  } catch {
    // error message shown by Axios interceptor
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const registeredUsername = registerForm.username
    await authApi.register(registerForm)
    ElMessage.success('注册成功')
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerFormRef.value.resetFields()
    activeTab.value = 'login'
    loginForm.username = registeredUsername
  } catch {
    // error message shown by Axios interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const msg = sessionStorage.getItem('loginError')
  if (msg) {
    sessionStorage.removeItem('loginError')
    ElMessage.error(msg)
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}
.login-subtitle {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
}
.login-tabs :deep(.el-tabs__nav) {
  width: 100%;
}
.login-tabs :deep(.el-tabs__item) {
  width: 50%;
  text-align: center;
}
</style>
