<template>
  <div class="login-page">
    <div class="login-card">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="text" maxlength="11" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="form-options">
          <label><input type="checkbox" v-model="remember" /> 记住我</label>
        </div>
        <button type="submit" class="btn-primary btn-block" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <div class="extra-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
      <div class="dev-section">
        <div class="dev-divider"><span>开发模式（跳过 API）</span></div>
        <button class="btn-primary btn-block btn-dev" @click="devLogin" :disabled="loading">
          {{ loading ? '登录中...' : '一键登录（Mock 账号）' }}
        </button>
        <p class="dev-hint">手机号 13800138000 / 密码 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const form = reactive({ phone: '', password: '' })
const remember = ref(false)
const loading = ref(false)

async function handleLogin() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return alert('请输入正确的手机号')
  if (form.password.length < 6) return alert('密码至少6位')

  loading.value = true
  try {
    await auth.login(form.phone, form.password)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch {
    // 拦截器已弹提示
  } finally {
    loading.value = false
  }
}

function devLogin() {
  loading.value = true
  const mockToken = 'eyJhbGciOiJIUzI1NiJ9.dev-mock-token'
  const mockUser = { id: 1, phone: '13800138000', realName: '张三', avatar: '' }
  const mockAuth = useAuthStore()
  mockAuth.token = mockToken
  mockAuth.userInfo = mockUser
  localStorage.setItem('token', mockToken)
  localStorage.setItem('userInfo', JSON.stringify(mockUser))
  const redirect = route.query.redirect || '/'
  router.push(redirect)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
}

.login-card {
  width: 400px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 40px;
}

.login-card h2 {
  text-align: center;
  font-size: 22px;
  margin-bottom: 30px;
  color: var(--text);
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-light);
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
}

.form-options {
  margin-bottom: 18px;
  font-size: 13px;
  color: var(--text-muted);
}

.form-options input[type="checkbox"] {
  margin-right: 4px;
}

.btn-block {
  width: 100%;
  padding: 12px;
  font-size: 15px;
}

.extra-links {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
}

.extra-links a {
  color: var(--primary);
}

.extra-links a:hover {
  text-decoration: underline;
}

.dev-section {
  margin-top: 24px;
}

.dev-divider {
  text-align: center;
  margin-bottom: 12px;
  position: relative;
}

.dev-divider::before,
.dev-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background: var(--border);
}

.dev-divider::before {
  left: 0;
}

.dev-divider::after {
  right: 0;
}

.dev-divider span {
  font-size: 12px;
  color: var(--text-muted);
  background: var(--bg-white);
  padding: 0 10px;
  position: relative;
}

.btn-dev {
  background: #ff9800;
}

.btn-dev:hover {
  background: #f57c00;
}

.dev-hint {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}
</style>
