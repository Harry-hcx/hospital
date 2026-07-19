<template>
  <div class="my-review-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>我的评价</h2>
        <div class="review-list">
          <div class="review-card" v-for="r in reviews" :key="r.id">
            <div class="review-header">
              <span class="review-type">{{ r.orderType === 1 ? '挂号' : '咨询' }}</span>
              <span class="review-doctor">{{ r.doctorName }}</span>
              <RateStar :modelValue="r.rating" readonly :size="'14px'" />
              <span class="review-date">{{ r.createTime }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
          </div>
        </div>
        <div class="empty" v-if="reviews.length === 0">暂无评价</div>
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
import RateStar from '@/components/RateStar.vue'
import Pagination from '@/components/Pagination.vue'
import { getMyReviews } from '@/api/user'

const reviews = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const loadError = ref('')

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyReviews({ page: page.value, pageSize: pageSize.value })
    const d = res.data.data || res.data
    reviews.value = d.list || []
    total.value = d.total || 0
  } catch (e) { loadError.value = '加载评价失败，请稍后重试'; console.error('加载评价列表失败', e) }
  finally { loading.value = false }
}

function handlePage(p) { page.value = p; fetchData() }
</script>

<style scoped>
.my-review-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { padding: 16px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
.review-type { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; }
.review-doctor { font-weight: 600; font-size: 14px; }
.review-date { font-size: 12px; color: var(--text-muted); margin-left: auto; }
.review-content { font-size: 14px; color: var(--text); line-height: 1.5; }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
