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
</style>
