<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main-content">
        <h2 class="page-title">我的关注</h2>

        <div class="tabs">
          <button v-for="t in tabs" :key="t.key" :class="{ active: activeType === t.key }" @click="switchTab(t.key)">
            {{ t.label }}
          </button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="follows.length === 0" class="empty">暂无关注</div>
        <div v-else class="follow-list">
          <div v-for="item in follows" :key="item.id" class="follow-card" @click="goDetail(item)">
            <div class="follow-avatar">
              <img v-if="item.image" :src="item.image" alt="" />
              <div v-else class="avatar-placeholder">?</div>
            </div>
            <div class="follow-info">
              <h4>{{ item.name || '--' }}</h4>
              <p v-if="item.level">{{ item.level }}</p>
              <p v-if="item.title">{{ item.title }}</p>
            </div>
            <button class="btn-unfollow" @click.stop="handleUnfollow(item)">取消关注</button>
          </div>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <button :disabled="page <= 1" @click="page--; fetchFollows()">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="page++; fetchFollows()">下一页</button>
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
import { getMyFollowsApi, unfollowApi } from '@/api/follow'

const router = useRouter()
const loading = ref(true)
const follows = ref([])
const activeType = ref(1)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const tabs = [
  { key: 1, label: '关注的医院' },
  { key: 2, label: '关注的医生' },
  { key: 3, label: '关注的疾病' },
]

onMounted(() => fetchFollows())

function switchTab(key) {
  activeType.value = key
  page.value = 1
  fetchFollows()
}

async function fetchFollows() {
  loading.value = true
  try {
    const res = await getMyFollowsApi({ type: activeType.value, page: page.value, pageSize: pageSize.value })
    if (res.data) {
      follows.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch { follows.value = [] }
  finally { loading.value = false }
}

function goDetail(item) {
  if (item.followType === 1) router.push(`/hospital/${item.followId}`)
  else if (item.followType === 2) router.push(`/doctor/${item.followId}`)
  else if (item.followType === 3) router.push(`/disease/${item.followId}`)
}

async function handleUnfollow(item) {
  if (!confirm('确定取消关注？')) return
  try {
    await unfollowApi(item.followType, item.followId)
    fetchFollows()
    alert('已取消关注')
  } catch { alert('操作失败') }
}
</script>

<style scoped>
.page-wrapper { min-height: 100vh; display: flex; flex-direction: column; }
.page-body { max-width: 1200px; margin: 20px auto; padding: 0 20px; display: flex; gap: 20px; flex: 1; width: 100%; }
.main-content { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 30px; }
.page-title { font-size: 20px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #1e88e5; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }

.tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.tabs button { padding: 8px 20px; background: #f5f5f5; border: 1px solid #e8e8e8; border-radius: 4px; cursor: pointer; font-size: 14px; color: #666; }
.tabs button.active { background: #1e88e5; color: #fff; border-color: #1e88e5; }

.follow-list { display: flex; flex-direction: column; gap: 12px; }
.follow-card { display: flex; align-items: center; gap: 16px; background: #f5f7fa; border-radius: 8px; padding: 16px 20px; cursor: pointer; transition: background 0.2s; }
.follow-card:hover { background: #eef0f4; }
.follow-avatar { width: 56px; height: 56px; border-radius: 8px; overflow: hidden; flex-shrink: 0; }
.follow-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { width: 100%; height: 100%; background: #e3f2fd; color: #1e88e5; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.follow-info { flex: 1; }
.follow-info h4 { font-size: 15px; margin-bottom: 2px; }
.follow-info p { font-size: 13px; color: #999; }
.btn-unfollow { padding: 6px 16px; background: #fff; color: #ff5252; border: 1px solid #ff5252; border-radius: 4px; font-size: 13px; cursor: pointer; }
.btn-unfollow:hover { background: #ff5252; color: #fff; }

.pagination { display: flex; gap: 12px; align-items: center; justify-content: center; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #e8e8e8; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
