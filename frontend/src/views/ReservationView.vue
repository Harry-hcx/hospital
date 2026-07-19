<template>
  <div class="reservation-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>预约挂号</span>
      </div>

      <div class="form-card">
        <h2>预约挂号</h2>

        <!-- 医生信息 -->
        <div class="doctor-info" v-if="doctor.id">
          <img :src="resolveImageUrl(doctor.avatar, 'doctor-male-doc.jpg') || defaultImg" :alt="doctor.name" />
          <div>
            <h4>{{ doctor.name }} <span class="title-tag">{{ doctor.title }}</span></h4>
            <p>{{ doctor.hospitalName }} · {{ doctor.departmentName }}</p>
          </div>
        </div>

        <!-- 排班选择 -->
        <div class="form-group">
          <label>选择排班</label>
          <div class="schedule-options">
            <div class="schedule-option" v-for="s in schedules" :key="s.id" :class="{ active: form.scheduleId === s.id }" @click="form.scheduleId = s.id">
              <div>{{ s.date }}</div>
              <div>{{ s.timeSlot }}</div>
              <div class="fee">¥{{ s.registrationPrice }}</div>
              <div>剩余 {{ s.remainCount }}</div>
            </div>
          </div>
          <div class="empty" v-if="schedules.length === 0">暂无排班</div>
        </div>

        <!-- 就诊人 -->
        <div class="form-group">
          <label>就诊人</label>
          <select v-model="form.familyMemberId">
            <option value="">请选择就诊人</option>
            <option v-for="m in members" :key="m.id" :value="m.id">{{ m.name }} ({{ m.relation }})</option>
          </select>
          <router-link to="/family-members" class="add-link">+ 添加就诊人</router-link>
        </div>

        <!-- 病情描述 -->
        <div class="form-group">
          <label>病情描述</label>
          <textarea v-model="form.diseaseDesc" rows="4" placeholder="请简要描述您的病情..."></textarea>
        </div>

        <button class="btn-primary btn-submit" @click="handleSubmit" :disabled="submitting">
          {{ submitting ? '提交中...' : '确认预约' }}
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
import { getDoctorDetail, getDoctorSchedules } from '@/api/doctor'
import { getFamilyMembers } from '@/api/user'
import { createAppointment } from '@/api/appointment'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const router = useRouter()
const doctor = ref({})
const schedules = ref([])
const members = ref([])
const submitting = ref(false)
const loading = ref(false)
const loadError = ref('')
const defaultImg = resolveImageUrl('doctor-male-doc.jpg', 'doctor-male-doc.jpg')

const form = ref({
  scheduleId: route.query.scheduleId ? Number(route.query.scheduleId) : '',
  familyMemberId: '',
  diseaseDesc: ''
})

onMounted(async () => {
  const doctorId = route.query.doctorId
  if (doctorId) {
    try {
      const [dRes, sRes, mRes] = await Promise.all([
        getDoctorDetail(doctorId),
        getDoctorSchedules(doctorId, { days: 7 }),
        getFamilyMembers()
      ])
      doctor.value = dRes?.data || {}
      schedules.value = sRes?.data || []
      members.value = mRes?.data || []
    } catch (e) { console.error('加载预约信息失败', e) }
  }
})

async function handleSubmit() {
  if (!doctor.value.id) { alert('医生信息未加载完成'); return }
  if (!form.value.scheduleId) { alert('请选择排班'); return }
  if (!form.value.familyMemberId) { alert('请选择就诊人'); return }
  const selectedSchedule = schedules.value.find((schedule) => Number(schedule.id) === Number(form.value.scheduleId))
  if (!selectedSchedule) { alert('所选排班不存在，请重新选择'); return }
  submitting.value = true
  try {
    const res = await createAppointment({
      doctorId: Number(doctor.value.id),
      hospitalId: Number(doctor.value.hospitalId),
      patientId: Number(form.value.familyMemberId),
      appointmentDate: selectedSchedule.date,
      appointmentTime: selectedSchedule.timeSlot,
      diseaseDesc: form.value.diseaseDesc
    })
    const d = res?.data || {}
    const orderNo = d.orderNo || d.id
    if (!orderNo) { alert('预约创建成功但未返回订单号'); return }
    router.push(`/reservation/pay/${orderNo}`)
  } catch (e) {
    console.error('预约失败', e)
    alert('预约失败，请重试')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.reservation-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.form-card { max-width: 700px; margin: 0 auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.form-card h2 { margin-bottom: 24px; font-size: 22px; }
.doctor-info { display: flex; gap: 16px; align-items: center; padding: 16px; background: var(--bg); border-radius: var(--radius); margin-bottom: 24px; }
.doctor-info img { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; }
.doctor-info h4 { font-size: 16px; }
.title-tag { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; margin-left: 6px; }
.doctor-info p { font-size: 13px; color: var(--text-light); }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--text); }
.schedule-options { display: flex; flex-wrap: wrap; gap: 10px; }
.schedule-option {
  padding: 12px 16px; border: 2px solid var(--border); border-radius: var(--radius);
  cursor: pointer; text-align: center; min-width: 120px; transition: all 0.2s; font-size: 13px;
}
.schedule-option:hover { border-color: var(--primary); }
.schedule-option.active { border-color: var(--primary); background: #e3f2fd; }
.schedule-option .fee { font-size: 16px; color: #e53935; font-weight: 600; }
.form-group select, .form-group textarea {
  width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px;
}
.form-group textarea { resize: vertical; }
.add-link { font-size: 13px; color: var(--primary); margin-left: 12px; }
.btn-submit { width: 100%; padding: 14px; font-size: 16px; margin-top: 16px; }
.empty { padding: 20px; text-align: center; color: var(--text-muted); }
</style>
