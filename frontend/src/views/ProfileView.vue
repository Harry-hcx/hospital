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
    const d = res?.data || {}
    if (d) Object.assign(form.value, d)
  } catch (e) { console.error('加载个人信息失败', e) }
})

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ ...form.value, gender: form.value.gender === 0 ? null : Number(form.value.gender) })
    alert('保存成功')
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败')
  } finally { saving.value = false }
}
</script>

<style scoped>
.profile-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 20px; }
.form-card { background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; max-width: 600px; }
.avatar-section { text-align: center; margin-bottom: 24px; }
.avatar-section img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; display: block; margin: 0 auto 8px; }
.btn-change { font-size: 13px; color: var(--primary); background: none; border: none; cursor: pointer; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
