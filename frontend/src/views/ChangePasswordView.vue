<template>
  <div class="change-password-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>修改密码</h2>
        <div class="form-card">
          <form @submit.prevent="handleSubmit" class="password-form">
            <div class="form-group">
              <label>原密码</label>
              <input v-model="form.oldPassword" type="password" placeholder="请输入原密码" />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input v-model="form.newPassword" type="password" placeholder="6-20位新密码" />
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
            </div>
            <button type="submit" class="btn-primary" :disabled="loading">
              {{ loading ? '提交中...' : '确认修改' }}
            </button>
          </form>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { changePasswordApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import AppFooter from '@/components/AppFooter.vue'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const loading = ref(false)

async function handleSubmit() {
  if (form.newPassword.length < 6) return alert('新密码至少6位')
  if (form.newPassword !== form.confirmPassword) return alert('两次新密码不一致')

  loading.value = true
  try {
    await changePasswordApi({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })
    alert('密码修改成功，请重新登录')
    auth.logout()
    router.push('/login?redirect=/')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.change-password-page { min-height: 100vh; background: var(--bg); }
.page-body { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 24px; width: 100%; align-items: start; }
.page-body :deep(.sidebar) { position: sticky; top: var(--header-height); left: 0; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center; padding-top: 24px; }
.main h2 { width: min(100%, 480px); font-size: 20px; margin-bottom: 20px; text-align: center; }
.form-card { width: min(100%, 480px); background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }

.password-form { width: 100%; }

.password-form .form-group {
  margin-bottom: 18px;
}

.password-form label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-light);
}

.password-form input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-body { grid-template-columns: minmax(0, 1fr); padding: 16px; }
  .page-body :deep(.sidebar) { position: static; }
  .main { padding-top: 0; }
  .main h2, .form-card { width: 100%; }
}
</style>
