<template>
  <div class="consult-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>在线咨询</span>
      </div>
      <div class="form-card">
        <h2>在线咨询</h2>
        <div class="doctor-info" v-if="doctor.id">
          <img :src="resolveImageUrl(doctor.avatar, 'doctor-male-doc.jpg') || defaultImg" :alt="doctor.name" />
          <div>
            <h4>{{ doctor.name }} <span class="title-tag">{{ doctor.title }}</span></h4>
            <p>{{ doctor.hospitalName }} · {{ doctor.departmentName }}</p>
            <p class="fee">咨询费：¥{{ doctor.consultPrice || 0 }}</p>
          </div>
        </div>
        <div class="form-group">
          <label>就诊人</label>
          <select v-model="form.familyMemberId">
            <option value="">请选择就诊人</option>
            <option v-for="m in members" :key="m.id" :value="m.id">{{ m.name }} ({{ m.relation }})</option>
          </select>
          <router-link to="/family-members" class="add-link">+ 添加就诊人</router-link>
        </div>
        <div class="form-group">
          <label>病情描述</label>
          <textarea v-model="form.diseaseDesc" rows="4" placeholder="请详细描述您的症状、持续时间等..."></textarea>
        </div>
        <div class="form-group">
          <label>预约时间</label>
          <input v-model="form.appointmentTime" type="datetime-local" />
        </div>
        <div class="form-group">
          <label>咨询时长</label>
          <select v-model.number="form.duration">
            <option :value="15">15 分钟</option>
            <option :value="30">30 分钟</option>
            <option :value="60">60 分钟</option>
          </select>
        </div>
        <button class="btn-primary btn-submit" @click="handleSubmit" :disabled="submitting">
          {{ submitting ? '提交中...' : '确认咨询' }}
        </button>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getDoctorDetail } from '@/api/doctor'
import { getFamilyMembers } from '@/api/user'
import { createConsult } from '@/api/consult'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const router = useRouter()
const doctor = ref({})
const members = ref([])
const submitting = ref(false)
const loading = ref(false)
const loadError = ref('')
const defaultImg = resolveImageUrl('doctor-male-doc.jpg', 'doctor-male-doc.jpg')

const form = ref({ familyMemberId: '', diseaseDesc: '', appointmentTime: '', duration: 30 })

onMounted(async () => {
  const doctorId = route.query.doctorId
  if (doctorId) {
    try {
      const [dRes, mRes] = await Promise.all([getDoctorDetail(doctorId), getFamilyMembers()])
      doctor.value = dRes?.data || {}
      members.value = mRes?.data || []
    } catch (e) { console.error('加载咨询信息失败', e) }
  }
})

async function handleSubmit() {
  if (!doctor.value.id) { alert('医生信息未加载完成'); return }
  if (!form.value.familyMemberId) { alert('请选择就诊人'); return }
  if (!form.value.appointmentTime) { alert('请选择预约时间'); return }
  const selectedMember = members.value.find((member) => Number(member.id) === Number(form.value.familyMemberId))
  if (!selectedMember) { alert('所选就诊人不存在，请重新选择'); return }
  submitting.value = true
  try {
    const res = await createConsult({
      doctorId: Number(doctor.value.id),
      patientName: selectedMember.name,
      patientPhone: selectedMember.phone,
      diseaseDesc: form.value.diseaseDesc,
      appointmentTime: `${form.value.appointmentTime.replace('T', ' ')}:00`,
      duration: Number(form.value.duration)
    })
    const d = res?.data || {}
    const orderNo = d.orderNo || d.id
    if (!orderNo) { alert('咨询创建成功但未返回订单号'); return }
    router.push(`/consult/pay/${orderNo}`)
  } catch (e) {
    console.error('咨询提交失败', e)
    alert('提交失败，请重试')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.consult-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.form-card { max-width: 700px; margin: 0 auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.form-card h2 { font-size: 22px; margin-bottom: 24px; }
.doctor-info { display: flex; gap: 16px; align-items: center; padding: 16px; background: var(--bg); border-radius: var(--radius); margin-bottom: 24px; }
.doctor-info img { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; }
.doctor-info h4 { font-size: 16px; }
.title-tag { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; margin-left: 6px; }
.doctor-info p { font-size: 13px; color: var(--text-light); }
.fee { color: #e53935; font-weight: 600; font-size: 14px; margin-top: 4px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 8px; }
.form-group select, .form-group textarea, .form-group input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; }
.form-group textarea { resize: vertical; }
.add-link { font-size: 13px; color: var(--primary); margin-left: 12px; }
.btn-submit { width: 100%; padding: 14px; font-size: 16px; margin-top: 16px; }
</style>
