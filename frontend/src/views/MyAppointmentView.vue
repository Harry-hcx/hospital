<template>
  <div class="my-appointment-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>我的挂号</h2>
        <div class="tabs">
          <button v-for="t in tabs" :key="t.value" :class="{ active: status === t.value }" @click="switchTab(t.value)">{{ t.label }}</button>
        </div>
        <div class="order-list">
          <div class="order-card" v-for="o in orders" :key="o.id">
            <div class="order-header">
              <span class="order-no">订单号：{{ o.orderNo }}</span>
              <span class="status" :class="'status-' + o.status">{{ statusMap[o.status] || '未知' }}</span>
            </div>
            <div class="order-body">
              <p>医生：{{ o.doctorName }}</p>
              <p>医院：{{ o.hospitalName }}</p>
              <p>就诊时间：{{ o.appointmentDate }} {{ o.appointmentTime }}</p>
              <p>就诊人：{{ o.patientName }}</p>
              <p class="fee">¥{{ o.amount }}</p>
            </div>
            <div class="order-actions" v-if="o.status === 1">
              <button class="btn-primary" @click="$router.push(`/reservation/pay/${o.orderNo}`)">去支付</button>
              <button class="btn-cancel" @click="handleCancel(o.orderNo)">取消预约</button>
            </div>
            <div class="review-form" v-if="o.status === 3 && reviewedOrderIds.has(o.id)">已评价</div>
            <div class="review-form" v-else-if="o.status === 3">
              <button v-if="reviewingId !== o.id" class="btn-review" @click="openReview(o)">评价医生</button>
              <div v-else>
                <RateStar v-model="reviewRating" :size="'20px'" />
                <textarea v-model="reviewContent" rows="3" placeholder="请输入评价"></textarea>
                <button class="btn-primary" :disabled="reviewSubmitting" @click="submitReview(o, 1)">提交评价</button>
              </div>
            </div>
          </div>
        </div>
        <div class="empty" v-if="orders.length === 0">暂无预约记录</div>
        <Pagination :total="total" :current="page" :pageSize="pageSize" @change="handlePage" />
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
import Pagination from '@/components/Pagination.vue'
import { getMyAppointments, cancelAppointment } from '@/api/appointment'
import { createReview, getMyReviews } from '@/api/user'
import RateStar from '@/components/RateStar.vue'

const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const status = ref('')
const reviewingId = ref(null)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewSubmitting = ref(false)
const reviewedOrderIds = ref(new Set())
const tabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 1 },
  { label: '已预约', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]
const statusMap = { 1: '待支付', 2: '已预约', 3: '已完成', 4: '已取消' }

onMounted(fetchData)

async function fetchData() {
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (status.value !== '') params.status = status.value
    const [res, reviewRes] = await Promise.all([
      getMyAppointments(params),
      getMyReviews({ page: 1, pageSize: 1000 }),
    ])
    const d = res.data.data || res.data
    orders.value = d.records || []
    total.value = d.total || 0
    const reviewData = reviewRes.data.data || reviewRes.data
    reviewedOrderIds.value = new Set((reviewData.records || [])
      .filter((item) => item.orderType === 1)
      .map((item) => item.orderId))
  } catch (e) { console.error('加载预约列表失败', e) }
}

function switchTab(v) { status.value = v; page.value = 1; fetchData() }
function handlePage(p) { page.value = p; fetchData() }

function openReview(order) {
  reviewingId.value = order.id
  reviewRating.value = 5
  reviewContent.value = ''
}

async function submitReview(order, orderType) {
  if (!reviewContent.value.trim()) { alert('请输入评价内容'); return }
  reviewSubmitting.value = true
  try {
    await createReview({ orderType, orderId: order.id, doctorId: order.doctorId, rating: reviewRating.value, content: reviewContent.value.trim() })
    reviewedOrderIds.value = new Set([...reviewedOrderIds.value, order.id])
    reviewingId.value = null
    alert('评价提交成功')
  } catch (e) { alert('评价提交失败，请稍后重试'); console.error('评价提交失败', e) }
  finally { reviewSubmitting.value = false }
}

async function handleCancel(orderNo) {
  if (!confirm('确认取消预约？')) return
  try {
    await cancelAppointment(orderNo)
    fetchData()
  } catch (e) { console.error('取消失败', e) }
}
</script>

<style scoped>
.my-appointment-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.tabs button { padding: 6px 16px; background: var(--bg-white); border: 1px solid var(--border); border-radius: 4px; font-size: 13px; cursor: pointer; }
.tabs button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-card { padding: 16px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); }
.order-header { display: flex; justify-content: space-between; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.order-no { font-size: 13px; color: var(--text-muted); }
.status-0 { color: #e53935; }
.status-1 { color: var(--primary); }
.status-2 { color: #4caf50; }
.status-3 { color: var(--text-muted); }
.order-body p { font-size: 13px; color: var(--text-light); padding: 2px 0; }
.fee { color: #e53935; font-weight: 600; font-size: 15px !important; }
.order-actions { margin-top: 10px; display: flex; gap: 8px; }
.btn-cancel { padding: 6px 16px; background: #fff; border: 1px solid #e53935; color: #e53935; border-radius: 4px; font-size: 13px; cursor: pointer; }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
