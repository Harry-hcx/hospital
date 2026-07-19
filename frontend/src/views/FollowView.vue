<template>
  <div class="follow-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>我的关注</h2>
        <div class="tabs">
          <button v-for="t in tabs" :key="t.type" :class="{ active: activeType === t.type }" @click="switchTab(t.type)">{{ t.label }}</button>
        </div>
        <div class="follow-list">
          <div class="follow-card" v-for="item in list" :key="item.id">
            <div class="follow-info">
              <h4>{{ item.name || item.title }}</h4>
              <p>关注对象 ID：{{ item.followId }}</p>
            </div>
            <button class="btn-unfollow" @click="handleUnfollow(item.followId)">取消关注</button>
          </div>
        </div>
        <div class="empty" v-if="list.length === 0">暂无关注</div>
        <Pagination :total="total" :current="page" :pageSize="pageSize" @change="handlePage" />
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
import AppSidebar from '@/components/AppSidebar.vue'
import Pagination from '@/components/Pagination.vue'
import { getMyFollows, deleteFollow } from '@/api/user'

const route = useRoute()
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeType = ref(Number(route.query.type) || 1)

const tabs = [
  { type: 1, label: '关注的医院' },
  { type: 2, label: '关注的医生' },
  { type: 3, label: '关注的疾病' }
]

onMounted(fetchData)

async function fetchData() {
  try {
    const res = await getMyFollows({ type: activeType.value, page: page.value, pageSize: pageSize.value })
    const d = res.data.data || res.data
    list.value = d.list || []
    total.value = d.total || 0
  } catch (e) { console.error('加载关注列表失败', e) }
}

function switchTab(type) { activeType.value = type; page.value = 1; fetchData() }
function handlePage(p) { page.value = p; fetchData() }

async function handleUnfollow(id) {
  if (!id) return
  try {
    await deleteFollow(activeType.value, id)
    fetchData()
  } catch (e) { console.error('取消关注失败', e) }
}
</script>

<style scoped>
.follow-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.tabs button { padding: 6px 16px; background: var(--bg-white); border: 1px solid var(--border); border-radius: 4px; font-size: 13px; cursor: pointer; }
.tabs button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.follow-list { display: flex; flex-direction: column; gap: 12px; }
.follow-card { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); }
.follow-info h4 { font-size: 16px; margin-bottom: 4px; }
.follow-info p { font-size: 13px; color: var(--text-light); }
.btn-unfollow { padding: 6px 16px; background: #fff; border: 1px solid var(--border); border-radius: 4px; font-size: 13px; cursor: pointer; color: var(--text-muted); }
.btn-unfollow:hover { border-color: #e53935; color: #e53935; }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
