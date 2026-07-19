<template>
  <div class="family-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>就诊人管理</h2>
        <button class="btn-primary btn-add" @click="showForm = true">+ 添加就诊人</button>

        <div class="member-list">
          <div class="member-card" v-for="m in members" :key="m.id">
            <div class="member-info">
              <h4>{{ m.name }} <span class="tag">{{ m.relation }}</span></h4>
              <p>{{ m.gender === 1 ? '男' : m.gender === 2 ? '女' : '未知' }} · {{ m.age || '' }}岁</p>
              <p>{{ m.phone }}</p>
            </div>
            <div class="member-actions">
              <button @click="editMember(m)">编辑</button>
              <button class="btn-del" @click="handleDelete(m.id)">删除</button>
            </div>
          </div>
        </div>
        <div class="empty" v-if="members.length === 0">暂无就诊人</div>

        <div class="modal" v-if="showForm" @click.self="showForm = false">
          <div class="modal-content">
            <h3>{{ editingId ? '编辑' : '添加' }}就诊人</h3>
            <div class="form-group"><label>姓名</label><input v-model="form.name" /></div>
            <div class="form-group"><label>性别</label><select v-model="form.gender"><option value="1">男</option><option value="2">女</option></select></div>
            <div class="form-group"><label>生日</label><input type="date" v-model="form.birthday" /></div>
            <div class="form-group"><label>手机号</label><input v-model="form.phone" /></div>
            <div class="form-group"><label>身份证</label><input v-model="form.idCard" /></div>
            <div class="form-group"><label>关系</label><select v-model="form.relation"><option value="本人">本人</option><option value="配偶">配偶</option><option value="子女">子女</option><option value="父母">父母</option></select></div>
            <div class="modal-actions">
              <button @click="showForm = false">取消</button>
              <button class="btn-primary" @click="handleSave">保存</button>
            </div>
          </div>
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
import { getFamilyMembers, createFamilyMember, updateFamilyMember, deleteFamilyMember } from '@/api/user'

const members = ref([])
const showForm = ref(false)
const editingId = ref(null)
const loading = ref(false)
const loadError = ref('')
const form = ref({ name: '', gender: 1, birthday: '', phone: '', idCard: '', relation: '本人' })

onMounted(fetchMembers)

async function fetchMembers() {
  loading.value = true
  try {
    const res = await getFamilyMembers()
    members.value = (res.data.data || res.data) || []
  } catch (e) { loadError.value = '加载就诊人失败，请稍后重试'; console.error('加载就诊人失败', e) }
  finally { loading.value = false }
}

function editMember(m) {
  editingId.value = m.id
  form.value = { ...m }
  showForm.value = true
}

async function handleSave() {
  try {
    if (editingId.value) {
      await updateFamilyMember(editingId.value, { ...form.value, gender: Number(form.value.gender) })
    } else {
      await createFamilyMember({ ...form.value, gender: Number(form.value.gender) })
    }
    showForm.value = false
    editingId.value = null
    form.value = { name: '', gender: 1, birthday: '', phone: '', idCard: '', relation: '本人' }
    await fetchMembers()
  } catch (e) { console.error('保存失败', e); alert('保存失败') }
}

async function handleDelete(id) {
  if (!confirm('确认删除？')) return
  try {
    await deleteFamilyMember(id)
    await fetchMembers()
  } catch (e) { console.error('删除失败', e) }
}
</script>

<style scoped>
.family-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.btn-add { margin-bottom: 16px; }
.member-list { display: flex; flex-direction: column; gap: 12px; }
.member-card { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); }
.member-info h4 { font-size: 16px; margin-bottom: 4px; }
.tag { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; margin-left: 8px; }
.member-info p { font-size: 13px; color: var(--text-light); }
.member-actions button { padding: 4px 12px; font-size: 13px; border: 1px solid var(--border); border-radius: 4px; background: #fff; cursor: pointer; margin-left: 8px; }
.btn-del { color: #e53935; border-color: #e53935 !important; }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: #fff; border-radius: var(--radius); padding: 24px; width: 400px; max-width: 90vw; }
.modal-content h3 { margin-bottom: 16px; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 13px; margin-bottom: 4px; }
.form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; }
.modal-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 16px; }
.modal-actions button { padding: 8px 20px; border-radius: 4px; font-size: 14px; }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
