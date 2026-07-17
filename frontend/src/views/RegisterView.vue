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
            <button type="button" class="btn-captcha" @click="sendCaptcha" :disabled="captchaCountdown > 0">
              {{ captchaCountdown > 0 ? captchaCountdown + 's' : '获取验证码' }}
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'

const router = useRouter()
const form = reactive({ phone: '', captcha: '', password: '', confirmPassword: '' })
const captchaCountdown = ref(0)
const loading = ref(false)

function sendCaptcha() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return alert('请输入正确的手机号')
  captchaCountdown.value = 60
  const timer = setInterval(() => {
    captchaCountdown.value--
    if (captchaCountdown.value <= 0) clearInterval(timer)
  }, 1000)
  alert('验证码已发送（开发阶段固定使用 123456）')
}

async function handleRegister() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return alert('请输入正确的手机号')
  if (form.password.length < 6) return alert('密码至少6位')
  if (form.password !== form.confirmPassword) return alert('两次密码不一致')
  if (!form.captcha) return alert('请输入验证码')

  loading.value = true
  try {
    await registerApi({
      phone: form.phone,
      password: form.password,
      confirmPassword: form.confirmPassword,
      captcha: form.captcha,
    })
    alert('注册成功！即将跳转登录页')
    router.push('/login')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
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

.form-group input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
}

.captcha-row {
  display: flex;
  gap: 10px;
}

.captcha-row input {
  flex: 1;
}

.btn-captcha {
  width: 120px;
  background: var(--bg);
  color: var(--primary);
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-captcha:hover:not(:disabled) {
  border-color: var(--primary);
}

.btn-captcha:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
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
