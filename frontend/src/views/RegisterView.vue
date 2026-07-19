<template>
  <div class="register-page">
    <div class="register-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="text" maxlength="11" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>验证码</label>
          <div class="captcha-row">
            <input v-model="form.captcha" type="text" placeholder="请输入验证码" />
            <button type="button" class="btn-captcha" @click="sendCaptcha" :disabled="sendingCaptcha || captchaCountdown > 0">
              {{ sendingCaptcha ? '发送中...' : captchaCountdown > 0 ? captchaCountdown + 's' : '获取验证码' }}
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>设置密码</label>
          <input v-model="form.password" type="password" placeholder="6-20位密码" />
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
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
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi, sendCaptchaApi } from '@/api/auth'

const router = useRouter()
const form = reactive({ phone: '', captcha: '', password: '', confirmPassword: '', realName: '', email: '', gender: '' })
const captchaCountdown = ref(0)
const sendingCaptcha = ref(false)
const loading = ref(false)
let captchaTimer = null

function startCaptchaCountdown(seconds = 60) {
  if (captchaTimer) clearInterval(captchaTimer)
  captchaCountdown.value = seconds
  captchaTimer = setInterval(() => {
    captchaCountdown.value--
    if (captchaCountdown.value <= 0) {
      clearInterval(captchaTimer)
      captchaTimer = null
      captchaCountdown.value = 0
    }
  }, 1000)
}

async function sendCaptcha() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    alert('请输入正确的手机号')
    return
  }
  if (sendingCaptcha.value || captchaCountdown.value > 0) {
    return
  }

  sendingCaptcha.value = true
  try {
    const res = await sendCaptchaApi({ phone: form.phone })
    const payload = res?.data || {}
    console.info('验证码发送成功', payload)
    alert(`验证码已发送，请留意短信${payload.cooldownSeconds ? `（${payload.cooldownSeconds} 秒后可重发）` : ''}`)
    startCaptchaCountdown(payload.cooldownSeconds || 60)
  } catch (error) {
    console.error('验证码发送失败', error)
    if (typeof error?.code !== 'number') {
      alert(error?.response?.data?.message || error?.message || '验证码发送失败，请稍后重试')
    }
  } finally {
    sendingCaptcha.value = false
  }
}

async function handleRegister() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return alert('请输入正确的手机号')
  if (form.password.length < 6) return alert('密码至少6位')
  if (form.password !== form.confirmPassword) return alert('两次密码不一致')
  if (!form.gender) return alert('请选择性别')

  loading.value = true
  try {
    await registerApi({
      username: form.phone,
      phone: form.phone,
      captcha: form.captcha,
      password: form.password,
      confirmPassword: form.confirmPassword,
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
  if (captchaTimer) {
    clearInterval(captchaTimer)
    captchaTimer = null
  }
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
