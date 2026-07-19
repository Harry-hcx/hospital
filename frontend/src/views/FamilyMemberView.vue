<template>
  <div class="family-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>就诊人管理</h2>
        <button class="btn-primary btn-add" @click="openCreateForm">+ 添加就诊人</button>

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

        <div class="modal" v-if="showForm">
          <div class="modal-content">
            <h3>{{ editingId ? '编辑' : '添加' }}就诊人</h3>
            <div class="form-group"><label>姓名</label><input v-model="form.name" /></div>
            <div class="form-group"><label>性别</label><select v-model="form.gender"><option value="1">男</option><option value="2">女</option></select></div>
            <div class="form-group"><label>生日</label><input type="date" v-model="form.birthday" /></div>
            <div class="form-group"><label>手机号</label><input v-model="form.phone" /></div>
            <div class="form-group"><label>身份证</label><input v-model="form.idCard" /></div>
            <div class="form-group"><label>关系</label><select v-model="form.relation"><option v-for="relation in relationOptions" :key="relation" :value="relation">{{ relation }}</option></select></div>
            <div class="modal-actions">
              <button @click="closeForm">取消</button>
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
import { computed, ref, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { getFamilyMembers, createFamilyMember, updateFamilyMember, deleteFamilyMember } from '@/api/user'

const members = ref([])
const showForm = ref(false)
const editingId = ref(null)
const loading = ref(false)
const loadError = ref('')
const defaultForm = () => ({ name: '', gender: 1, birthday: '', phone: '', idCard: '', relation: '本人' })
const form = ref(defaultForm())
const baseRelations = ['本人', '配偶', '子女', '父母']
const relationOptions = computed(() => {
  const relation = form.value.relation
  return relation && !baseRelations.includes(relation) ? [...baseRelations, relation] : baseRelations
})

onMounted(fetchMembers)

async function fetchMembers() {
  loading.value = true
  try {
    const res = await getFamilyMembers()
    const data = unwrapResponseData(res) || []
    members.value = data.map(member => ({ ...member, age: memberAge(member) }))
  } catch (e) { loadError.value = '加载就诊人失败，请稍后重试'; console.error('加载就诊人失败', e) }
  finally { loading.value = false }
}

function openCreateForm() {
  editingId.value = null
  form.value = defaultForm()
  showForm.value = true
}

function editMember(m) {
  editingId.value = m.id
  form.value = {
    name: m.name || '',
    gender: Number(m.gender) === 2 ? 2 : 1,
    birthday: m.birthday || '',
    phone: m.phone || '',
    idCard: m.idCard || '',
    relation: m.relation || '本人',
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = defaultForm()
}

async function handleSave() {
  const validationMessage = validateMemberForm()
  if (validationMessage) {
    alert(validationMessage)
    return
  }
  try {
    if (editingId.value) {
      await updateFamilyMember(editingId.value, { ...form.value, gender: Number(form.value.gender) })
    } else {
      await createFamilyMember({ ...form.value, gender: Number(form.value.gender) })
    }
    closeForm()
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
function unwrapResponseData(res) {
  return res?.data?.data ?? res?.data ?? res
}

function memberAge(member) {
  if (member.age !== null && member.age !== undefined && member.age !== '') {
    return String(member.age)
  }
  if (!member.birthday) return ''
  const birthday = new Date(member.birthday)
  if (Number.isNaN(birthday.getTime())) return ''
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const beforeBirthday =
    today.getMonth() < birthday.getMonth()
    || (today.getMonth() === birthday.getMonth() && today.getDate() < birthday.getDate())
  if (beforeBirthday) age -= 1
  return age >= 0 ? String(age) : ''
}

function validateMemberForm() {
  const name = form.value.name?.trim()
  const phone = form.value.phone?.trim()
  const idCard = form.value.idCard?.trim()
  const relation = form.value.relation?.trim()
  if (!name || !/^[\u4e00-\u9fa5A-Za-z·]{2,20}$/.test(name)) return '请输入2-20位中文或英文姓名'
  if (![1, 2].includes(Number(form.value.gender))) return '请选择正确的性别'
  if (!form.value.birthday) return '请选择生日'
  const birthday = new Date(form.value.birthday)
  const today = new Date()
  const minDate = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate())
  if (Number.isNaN(birthday.getTime()) || birthday > today || birthday < minDate) return '生日不合法'
  if (!/^1\d{10}$/.test(phone || '')) return '请输入正确的11位手机号'
  if (idCard && !/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/.test(idCard)) {
    return '请输入正确的身份证号'
  }
  if (!relation || relation.length > 10) return '请选择关系'
  return ''
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
