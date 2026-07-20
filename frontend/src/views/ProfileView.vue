<template>
  <div class="profile-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>个人资料</h2>
        <div class="form-card">
          <div class="avatar-section">
            <img :src="resolveImageUrl(form.avatar, 'icons-pa.jpg') || defaultImg" :alt="form.realName" />
            <button class="btn-change">更换头像</button>
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="form.realName" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="form.gender">
              <option disabled :value="0">请选择性别</option>
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input v-model="form.phone" disabled />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" placeholder="请输入邮箱" />
          </div>
          <div class="form-group">
            <label>生日</label>
            <input type="date" v-model="form.birthday" />
          </div>
          <button class="btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { getProfile, updateProfile } from '@/api/user'
import { resolveImageUrl } from '@/utils/asset'

const defaultImg = resolveImageUrl('/avatar/user-1.png', 'icons-pa.jpg')
const form = ref({ realName: '', gender: 0, phone: '', email: '', birthday: '', avatar: '' })
const loading = ref(false)
const saving = ref(false)
const loadError = ref('')

onMounted(async () => {
  try {
    const res = await getProfile()
    const d = unwrapResponseData(res) || {}
    if (d) Object.assign(form.value, d)
  } catch (e) { console.error('加载个人信息失败', e) }
})

async function handleSave() {
  const validationMessage = validateProfileForm()
  if (validationMessage) {
    alert(validationMessage)
    return
  }
  saving.value = true
  try {
    await updateProfile({ ...form.value, gender: form.value.gender === 0 ? null : Number(form.value.gender) })
    alert('保存成功')
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败')
  } finally { saving.value = false }
}

function validateProfileForm() {
  const realName = form.value.realName?.trim()
  const email = form.value.email?.trim()
  const avatar = form.value.avatar?.trim()
  if (realName && !/^[\u4e00-\u9fa5A-Za-z·]{2,20}$/.test(realName)) return '姓名需为2-20位中文或英文'
  if (![0, 1, 2, null, undefined].includes(form.value.gender)) return '请选择正确的性别'
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return '请输入正确的邮箱'
  if (form.value.birthday) {
    const birthday = new Date(form.value.birthday)
    const today = new Date()
    const minDate = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate())
    if (Number.isNaN(birthday.getTime()) || birthday > today || birthday < minDate) return '生日不合法'
  }
  if (avatar && avatar.length > 255) return '头像地址过长'
  return ''
}

function unwrapResponseData(res) {
  return res?.data?.data ?? res?.data ?? res
}
</script>

<style scoped>
.profile-page { min-height: 100vh; background: var(--bg); }
.page-body { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 24px; width: 100%; align-items: start; }
.page-body :deep(.sidebar) { position: sticky; top: var(--header-height); left: 0; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center; padding-top: 24px; }
.main h2 { width: min(100%, 600px); font-size: 20px; margin-bottom: 20px; text-align: center; }
.form-card { width: min(100%, 600px); background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.avatar-section { text-align: center; margin-bottom: 24px; }
.avatar-section img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; display: block; margin: 0 auto 8px; }
.btn-change { font-size: 13px; color: var(--primary); background: none; border: none; cursor: pointer; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; }
@media (max-width: 768px) {
  .page-body { grid-template-columns: minmax(0, 1fr); padding: 16px; }
  .page-body :deep(.sidebar) { position: static; }
  .main { padding-top: 0; }
  .main h2, .form-card { width: 100%; }
}
</style>
