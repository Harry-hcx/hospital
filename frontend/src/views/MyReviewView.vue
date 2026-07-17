<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main-content">
        <h2 class="page-title">我的评价</h2>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="reviews.length === 0" class="empty">暂无评价</div>
        <div v-else class="review-list">
          <div v-for="item in reviews" :key="item.id" class="review-card">
            <div class="review-header">
              <span class="order-type">{{ item.orderType === 1 ? '挂号' : '咨询' }}</span>
              <span class="stars">{{ '★'.repeat(item.rating) }}{{ '☆'.repeat(5 - item.rating) }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <p class="review-content">{{ item.content || '无文字评价' }}</p>
          </div>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <button :disabled="page <= 1" @click="page--; fetchReviews()">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="page++; fetchReviews()">下一页</button>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getMyReviewsApi } from '@/api/review'

const loading = ref(true)
const reviews = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(() => fetchReviews())

async function fetchReviews() {
  loading.value = true
  try {
    const res = await getMyReviewsApi({ page: page.value, pageSize: pageSize.value })
    if (res.data) {
      reviews.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch { reviews.value = [] }
  finally { loading.value = false }
}
</script>

<style scoped>
.page-wrapper { min-height: 100vh; display: flex; flex-direction: column; }
.page-body { max-width: 1200px; margin: 20px auto; padding: 0 20px; display: flex; gap: 20px; flex: 1; width: 100%; }
.main-content { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 30px; }
.page-title { font-size: 20px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 2px solid #1e88e5; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }

.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { background: #f5f7fa; border-radius: 8px; padding: 16px 20px; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.order-type { display: inline-block; padding: 2px 8px; background: #e3f2fd; color: #1e88e5; border-radius: 4px; font-size: 12px; }
.stars { color: #ff9800; font-size: 16px; }
.time { color: #999; font-size: 12px; margin-left: auto; }
.review-content { color: #333; font-size: 14px; line-height: 1.6; }

.pagination { display: flex; gap: 12px; align-items: center; justify-content: center; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #e8e8e8; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
