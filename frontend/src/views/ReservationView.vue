<template>
  <div class="reservation-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> >
        <router-link to="/doctors">找医生</router-link> >
        <router-link v-if="doctor.id" :to="`/doctor/${doctor.id}`">{{ doctor.name }}</router-link> >
        <span>预约挂号</span>
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
            <div
              v-for="s in schedules"
              :key="s.id"
              class="schedule-option"
              :class="{ active: Number(form.scheduleId) === Number(s.id), disabled: !isScheduleAvailable(s) }"
              :data-status="scheduleStatusText(s)"
              @click="selectSchedule(s)"
            >
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
      doctor.value = unwrapResponseData(dRes) || {}
      schedules.value = unwrapResponseData(sRes) || []
      members.value = unwrapResponseData(mRes) || []
    } catch (e) { console.error('加载预约信息失败', e) }
  }
})

function selectSchedule(schedule) {
  if (!isScheduleAvailable(schedule)) return
  form.value.scheduleId = schedule.id
}

function isScheduleAvailable(schedule) {
  if (schedule?.isAvailable === false) return false
  if (!schedule?.date) return schedule?.isAvailable !== false
  const dateText = String(schedule.date).slice(0, 10)
  const todayText = formatDate(new Date())
  if (dateText < todayText) return false
  if (dateText > todayText) return schedule?.isAvailable !== false
  const endTime = parseScheduleEndTime(schedule.timeSlot)
  if (!endTime) return schedule?.isAvailable !== false
  const now = new Date()
  return endTime > now.getHours() * 60 + now.getMinutes()
}

function scheduleStatusText(schedule) {
  return Number(schedule?.remainCount || 0) <= 0 ? '已满' : '已过期'
}

function parseScheduleEndTime(timeSlot) {
  const matches = String(timeSlot || '').match(/\d{1,2}:\d{2}/g)
  if (!matches || matches.length === 0) return null
  const [hour, minute] = matches[matches.length - 1].split(':').map(Number)
  if (!Number.isInteger(hour) || !Number.isInteger(minute) || hour > 23 || minute > 59) return null
  return hour * 60 + minute
}

function formatDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

async function handleSubmit() {
  if (!doctor.value.id) { alert('医生信息未加载完成'); return }
  if (!form.value.scheduleId) { alert('请选择排班'); return }
  if (!form.value.familyMemberId) { alert('请选择就诊人'); return }
  const selectedSchedule = schedules.value.find((schedule) => Number(schedule.id) === Number(form.value.scheduleId))
  if (!selectedSchedule) { alert('所选排班不存在，请重新选择'); return }
  if (!isScheduleAvailable(selectedSchedule)) { alert('该排班已过期，请重新选择'); return }
  const selectedMember = members.value.find((member) => Number(member.id) === Number(form.value.familyMemberId))
  if (!selectedMember) { alert('所选就诊人不存在，请重新选择'); return }
  submitting.value = true
  try {
    const res = await createAppointment({
      doctorId: Number(doctor.value.id),
      hospitalId: Number(doctor.value.hospitalId),
      patientId: Number(selectedMember.id),
      familyMemberId: Number(selectedMember.id),
      patientName: selectedMember.name,
      patientPhone: selectedMember.phone,
      patientIdCard: selectedMember.idCard,
      patientGender: selectedMember.gender,
      patientBirthday: selectedMember.birthday,
      patientRelation: selectedMember.relation,
      appointmentDate: selectedSchedule.date,
      appointmentTime: selectedSchedule.timeSlot,
      diseaseDesc: form.value.diseaseDesc
    })
    const d = unwrapResponseData(res) || {}
    const orderNo = d.orderNo || d.id
    if (!orderNo) { alert('预约创建成功但未返回订单号'); return }
    router.push(`/reservation/pay/${orderNo}`)
  } catch (e) {
    console.error('预约失败', e)
    alert('预约失败，请重试')
  } finally { submitting.value = false }
}

function unwrapResponseData(res) {
  return res?.data?.data ?? res?.data ?? res
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
.schedule-option.disabled { background: #f7f7f7; border-color: #ddd; color: var(--text-muted); cursor: not-allowed; }
.schedule-option.disabled:hover { border-color: #ddd; }
.schedule-option.disabled::after { content: attr(data-status); display: inline-block; margin-top: 6px; padding: 3px 10px; background: #e0e0e0; color: #666; border-radius: 4px; font-size: 12px; }
.schedule-option .fee { font-size: 16px; color: #e53935; font-weight: 600; }
.schedule-option.disabled .fee { color: var(--text-muted); }
.form-group select, .form-group textarea {
  width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px;
}
.form-group textarea { resize: vertical; }
.add-link { font-size: 13px; color: var(--primary); margin-left: 12px; }
.btn-submit { width: 100%; padding: 14px; font-size: 16px; margin-top: 16px; }
.empty { padding: 20px; text-align: center; color: var(--text-muted); }
</style>
