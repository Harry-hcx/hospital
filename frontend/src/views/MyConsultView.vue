<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main-content">
        <h2 class="page-title">我的咨询订单</h2>
        
        <div class="tabs">
          <button v-for="t in tabs" :key="t.key" :class="{ active: activeTab === t.key }" @click="activeTab = t.key; page = 1; fetchOrders()">
            {{ t.label }}
          </button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="orders.length === 0" class="empty">暂无订单</div>
        <div v-else class="order-list">
          <div v-for="order in orders" :key="order.orderNo" class="order-card">
            <div class="order-info">
              <h4>{{ order.doctorName || '--' }}</h4>
              <p>咨询人：{{ order.patientName || '--' }}</p>
              <p>时间：{{ order.appointmentTime }}</p>
              <p>时长：{{ order.duration }}分钟</p>
              <p class="amount">{{ order.amount ? '¥' + order.amount : '' }}</p>
              <span class="status" :class="'status-' + order.status">{{ statusMap[order.status] || '--' }}</span>
            </div>
            <div class="order-actions">
              <button v-if="order.status === 1" class="btn-primary" @click="goPay(order.orderNo)">去支付</button>
              <button v-if="order.status === 5" class="btn-cancel" @click="handleCancel(order.orderNo)">取消</button>
              <button v-if="order.status === 4" class="btn-review" @click="openReview(order)">去评价</button>
            </div>
          </div>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <button :disabled="page <= 1" @click="page--; fetchOrders()">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="page++; fetchOrders()">下一页</button>
        </div>

        <div v-if="showReview" class="modal-overlay" @click.self="showReview = false">
          <div class="modal">
            <h3>评价医生</h3>
            <p class="review-doctor">{{ reviewTarget.doctorName }}</p>
            <div class="star-row">
              <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= reviewRating }" @click="reviewRating = s">★</span>
            </div>
            <textarea v-model="reviewContent" placeholder="请输入评价内容" class="review-text"></textarea>
            <div class="modal-btns">
              <button class="btn-primary" @click="submitReview" :disabled="submitting">提交</button>
              <button class="btn-cancel" @click="showReview = false">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getMyConsultsApi, cancelConsultApi } from '@/api/consult'
import { addReviewApi } from '@/api/review'

const router = useRouter()
const loading = ref(true)
const orders = ref([])
const activeTab = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const tabs = [
  { key: '', label: '全部' },
  { key: '1', label: '待支付' },
  { key: '2', label: '已支付' },
  { key: '3', label: '咨询中' },
  { key: '4', label: '已完成' },
  { key: '5', label: '已取消' },
]
const statusMap = { 1: '待支付', 2: '已支付', 3: '咨询中', 4: '已完成', 5: '已取消' }

const showReview = ref(false)
const reviewTarget = ref({})
const reviewRating = ref(5)
const reviewContent = ref('')
const submitting = ref(false)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (activeTab.value) params.status = activeTab.value
    const res = await getMyConsultsApi(params)
    if (res.data) {
      orders.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch { orders.value = [] }
  finally { loading.value = false }
}

function goPay(orderNo) {
  router.push(`/consult/pay/${orderNo}`)
}

async function handleCancel(orderNo) {
  if (!confirm('确定要取消该订单吗？')) return
  try {
    await cancelConsultApi(orderNo)
    alert('取消成功')
    fetchOrders()
  } catch { alert('取消失败') }
}

function openReview(order) {
  reviewTarget.value = order
  reviewRating.value = 5
  reviewContent.value = ''
  showReview.value = true
}

async function submitReview() {
  submitting.value = true
  try {
    await addReviewApi({
      orderType: 2,
      orderId: reviewTarget.value.id,
      doctorId: reviewTarget.value.doctorId,
      rating: reviewRating.value,
      content: reviewContent.value
    })
    showReview.value = false
    alert('评价成功')
  } catch { alert('评价失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.page-wrapper { min-height: 100vh; display: flex; flex-direction: column; }
.page-body { max-width: 1200px; margin: 20px auto; padding: 0 20px; display: flex; gap: 20px; flex: 1; width: 100%; }
.main-content { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 30px; }
.page-title { font-size: 20px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #1e88e5; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }

.tabs { display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.tabs button { padding: 6px 16px; background: #f5f5f5; border: 1px solid #e8e8e8; border-radius: 4px; cursor: pointer; font-size: 13px; color: #666; }
.tabs button.active { background: #1e88e5; color: #fff; border-color: #1e88e5; }

.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-card { display: flex; justify-content: space-between; align-items: center; background: #f5f7fa; border-radius: 8px; padding: 16px 20px; }
.order-info h4 { font-size: 15px; margin-bottom: 4px; }
.order-info p { font-size: 13px; color: #666; line-height: 1.8; }
.amount { color: #ff5252; font-weight: bold; }
.status { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-1 { background: #fff3e0; color: #ff9800; }
.status-2 { background: #e3f2fd; color: #1e88e5; }
.status-3 { background: #ede7f6; color: #7c4dff; }
.status-4 { background: #e8f5e9; color: #4caf50; }
.status-5 { background: #f5f5f5; color: #999; }

.order-actions { display: flex; gap: 8px; }
.btn-primary { padding: 6px 16px; background: #1e88e5; color: #fff; border-radius: 4px; font-size: 13px; border: none; cursor: pointer; }
.btn-cancel { padding: 6px 16px; background: #fff; color: #ff5252; border: 1px solid #ff5252; border-radius: 4px; font-size: 13px; cursor: pointer; }
.btn-review { padding: 6px 16px; background: #ff9800; color: #fff; border-radius: 4px; font-size: 13px; border: none; cursor: pointer; }

.pagination { display: flex; gap: 12px; align-items: center; justify-content: center; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #e8e8e8; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: #fff; border-radius: 8px; padding: 30px; width: 420px; }
.modal h3 { font-size: 18px; margin-bottom: 16px; }
.review-doctor { color: #1e88e5; font-size: 15px; margin-bottom: 12px; }
.star-row { margin-bottom: 16px; }
.star { font-size: 28px; color: #ddd; cursor: pointer; }
.star.active { color: #ff9800; }
.review-text { width: 100%; height: 100px; padding: 10px; border: 1px solid #e8e8e8; border-radius: 4px; resize: vertical; margin-bottom: 20px; font-size: 14px; }
.modal-btns { display: flex; gap: 12px; justify-content: flex-end; }
</style>
