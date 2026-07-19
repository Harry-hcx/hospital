<template>
  <div class="register-page">
    <div class="register-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" maxlength="30" placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="text" maxlength="11" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>验证码</label>
          <div class="captcha-row">
            <input v-model="form.captcha" type="text" maxlength="6" placeholder="请输入验证码" />
            <button
              type="button"
              class="captcha-btn"
              :disabled="captchaSending || countdown > 0"
              @click="handleSendCaptcha"
            >
              {{ captchaButtonText }}
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>设置密码</label>
          <input v-model="form.password" type="password" placeholder="6-20位密码" />
        </div>
        <div class="form-group">
          <label>真实姓名（选填）</label>
          <input v-model="form.realName" type="text" placeholder="请输入真实姓名" />
        </div>
        <div class="form-group">
          <label>邮箱（选填）</label>
          <input v-model="form.email" type="email" placeholder="请输入邮箱" />
        </div>
        <div class="form-group">
          <label>性别</label>
          <select v-model="form.gender">
            <option disabled value="">请选择性别</option>
            <option value="1">男</option>
            <option value="2">女</option>
          </select>
        </div>
        <button type="submit" class="btn-primary btn-block" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
      <div class="extra-links">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi, sendCaptchaApi } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', phone: '', captcha: '', password: '', realName: '', email: '', gender: '' })
const loading = ref(false)
const captchaSending = ref(false)
const countdown = ref(0)

let countdownTimer = null

const captchaButtonText = computed(() => {
  if (captchaSending.value) return '发送中...'
  if (countdown.value > 0) return `${countdown.value}s后重试`
  return '获取验证码'
})

function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

function startCountdown(seconds = 60) {
  clearCountdown()
  countdown.value = seconds
  countdownTimer = window.setInterval(() => {
    if (countdown.value <= 1) {
      clearCountdown()
      return
    }
    countdown.value -= 1
  }, 1000)
}

function clearCountdown() {
  if (countdownTimer !== null) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

async function handleSendCaptcha() {
  if (!isValidPhone(form.phone)) return alert('请先输入正确的手机号')

  captchaSending.value = true
  try {
    const response = await sendCaptchaApi({ phone: form.phone })
    startCountdown(response?.data?.cooldownSeconds || 60)
    alert('验证码已发送，请注意查收')
  } catch {
    // 拦截器已提示
  } finally {
    captchaSending.value = false
  }
}

async function handleRegister() {
  if (!form.username.trim()) return alert('请输入用户名')
  if (!isValidPhone(form.phone)) return alert('请输入正确的手机号')
  if (!form.captcha.trim()) return alert('请输入验证码')
  if (form.password.length < 6) return alert('密码至少6位')
  if (!form.gender) return alert('请选择性别')

  loading.value = true
  try {
    await registerApi({
      username: form.username.trim(),
      phone: form.phone,
      password: form.password,
      realName: form.realName || undefined,
      email: form.email || undefined,
      gender: Number(form.gender),
    })
    alert('注册成功！即将跳转登录页')
    router.push('/login')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  clearCountdown()
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
}

.register-card {
  width: 420px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 40px;
}

.register-card h2 {
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-row input {
  flex: 1;
}

.captcha-btn {
  width: 120px;
  flex-shrink: 0;
  border: 1px solid var(--primary);
  border-radius: 4px;
  background: #fff;
  color: var(--primary);
  transition: all 0.2s;
}

.captcha-btn:hover:not(:disabled) {
  background: var(--primary-light);
}

.captcha-btn:disabled {
  border-color: var(--border);
  color: var(--text-light);
  background: #f5f5f5;
  cursor: not-allowed;
}

.form-group select {
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--bg-white);
  color: var(--text);
  cursor: pointer;
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
