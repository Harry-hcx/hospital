<template>
  <div class="doctor-detail-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <router-link to="/doctors">找医生</router-link> > <span>{{ doctor.name }}</span>
      </div>

      <div class="detail-header" v-if="doctor.id">
        <img :src="resolveImageUrl(doctor.avatar, 'doctor-male-doc.jpg') || defaultImg" :alt="doctor.name" class="header-img" />
        <div class="header-info">
          <h2>{{ doctor.name }} <span class="title-tag">{{ doctor.title }}</span></h2>
          <p class="dept">{{ doctor.hospitalName }} · {{ doctor.departmentName }}</p>
          <p class="desc">{{ doctor.intro || '暂无简介' }}</p>
          <div class="actions">
            <RateStar :modelValue="doctor.rating || 0" readonly :size="'16px'" showText />
            <button class="btn-follow" @click="toggleFollow">{{ isFollowed ? '已关注' : '+ 关注' }}</button>
            <router-link :to="`/reservation?doctorId=${doctor.id}`" class="btn-primary">预约挂号</router-link>
            <router-link :to="`/consult?doctorId=${doctor.id}`" class="btn-consult">在线咨询</router-link>
          </div>
        </div>
      </div>

      <!-- 排班 -->
      <div class="section">
        <h3>出诊安排</h3>
        <div class="schedule-grid">
          <div class="schedule-card" v-for="s in schedules" :key="s.id">
            <div class="s-date">{{ s.date }}</div>
            <div class="s-period">{{ s.timeSlot }}</div>
            <div class="s-fee">¥{{ s.registrationPrice }}</div>
            <div class="s-num">剩余 {{ s.remainCount || 0 }} 号</div>
            <router-link :to="`/reservation?doctorId=${doctor.id}&scheduleId=${s.id}`" class="btn-book">预约</router-link>
          </div>
        </div>
        <div class="empty" v-if="schedules.length === 0">暂无排班信息</div>
      </div>

      <!-- 评价 -->
      <div class="section">
        <h3>患者评价</h3>
        <div class="review-list">
          <div class="review-item" v-for="r in reviews" :key="r.id">
            <div class="review-header">
              <span class="review-user">{{ r.userName || '匿名用户' }}</span>
              <RateStar :modelValue="r.rating" readonly :size="'14px'" />
              <span class="review-date">{{ r.createTime }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
          </div>
        </div>
        <div class="empty" v-if="reviews.length === 0">暂无评价</div>
        <Pagination :total="reviewTotal" :current="reviewPage" :pageSize="reviewPageSize" @change="handleReviewPage" />
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import RateStar from '@/components/RateStar.vue'
import Pagination from '@/components/Pagination.vue'
import { getDoctorDetail, getDoctorSchedules, getDoctorReviews } from '@/api/doctor'
import { createFollow, deleteFollow, getMyFollows } from '@/api/user'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const doctor = ref({})
const schedules = ref([])
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const isFollowed = ref(false)
const defaultImg = resolveImageUrl('doctor-male-doc.jpg', 'doctor-male-doc.jpg')

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getDoctorDetail(id)
    doctor.value = res.data.data || res.data
  } catch (e) { console.error('加载医生详情失败', e) }
  try {
    const res = await getDoctorSchedules(id, { days: 7 })
    schedules.value = (res.data.data || res.data) || []
  } catch (e) { /* ignore */ }
  if (localStorage.getItem('token')) {
    try {
      const res = await getMyFollows({ type: 2, page: 1, pageSize: 1000 })
      const d = res.data.data || res.data
      isFollowed.value = (d.list || []).some(item => Number(item.followId) === Number(id))
    } catch (e) { /* ignore */ }
  }
  fetchReviews()
})

async function fetchReviews() {
  try {
    const res = await getDoctorReviews(route.params.id, { page: reviewPage.value, pageSize: reviewPageSize.value })
    const d = res.data.data || res.data
    reviews.value = d.list || []
    reviewTotal.value = d.total || 0
  } catch (e) { console.error('加载评价失败', e) }
}

function handleReviewPage(p) { reviewPage.value = p; fetchReviews() }

async function toggleFollow() {
  try {
    if (isFollowed.value) {
      await deleteFollow(2, doctor.value.id)
      isFollowed.value = false
    } else {
      await createFollow(2, doctor.value.id)
      isFollowed.value = true
    }
  } catch (e) { console.error('关注操作失败', e) }
}
</script>

<style scoped>
.doctor-detail-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.detail-header {
  display: flex; gap: 24px; padding: 24px; background: var(--bg-white);
  border-radius: var(--radius); box-shadow: var(--shadow); margin-bottom: 24px;
}
.header-img { width: 160px; height: 160px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.header-info { flex: 1; display: flex; flex-direction: column; gap: 10px; }
.header-info h2 { font-size: 24px; }
.title-tag { font-size: 12px; padding: 2px 8px; background: #e3f2fd; color: var(--primary); border-radius: 3px; font-weight: normal; margin-left: 8px; }
.dept { font-size: 14px; color: var(--text-light); }
.desc { font-size: 14px; color: var(--text-muted); line-height: 1.6; }
.actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-top: 8px; }
.btn-follow { padding: 8px 20px; background: #fff; border: 1px solid var(--primary); color: var(--primary); border-radius: 4px; font-size: 14px; }
.btn-follow:hover { background: #e3f2fd; }
.btn-consult { padding: 10px 24px; background: #fff; border: 1px solid var(--primary); color: var(--primary); border-radius: 4px; font-size: 14px; }
.btn-consult:hover { background: #e3f2fd; }
.section { margin-bottom: 32px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 20px; }
.section h3 { font-size: 18px; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }
.schedule-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
.schedule-card { text-align: center; padding: 14px; border: 1px solid var(--border); border-radius: var(--radius); }
.s-date { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.s-period { font-size: 13px; color: var(--primary); margin-bottom: 4px; }
.s-fee { font-size: 16px; color: #e53935; font-weight: 600; margin-bottom: 4px; }
.s-num { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
.btn-book { display: inline-block; padding: 6px 20px; background: var(--primary); color: #fff; border-radius: 4px; font-size: 13px; }
.btn-book:hover { background: var(--primary-dark); }
.review-list { display: flex; flex-direction: column; gap: 14px; }
.review-item { padding: 14px; border: 1px solid var(--border); border-radius: var(--radius); }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.review-user { font-weight: 600; font-size: 14px; }
.review-date { font-size: 12px; color: var(--text-muted); margin-left: auto; }
.review-content { font-size: 14px; color: var(--text); line-height: 1.5; }
.empty { text-align: center; padding: 30px; color: var(--text-muted); }
@media (max-width: 600px) { .detail-header { flex-direction: column; align-items: center; } }
</style>
