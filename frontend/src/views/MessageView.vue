<template>
  <div class="message-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>消息中心</h2>
        <div class="message-list">
          <div class="message-card" v-for="m in messages" :key="m.id" :class="{ unread: !m.isRead }" @click="handleRead(m)">
            <div class="msg-header">
              <span class="msg-title">{{ m.title }}</span>
              <span class="msg-dot" v-if="!m.isRead"></span>
            </div>
            <p class="msg-content">{{ m.content }}</p>
            <span class="msg-time">{{ m.createTime }}</span>
          </div>
        </div>
        <div class="empty" v-if="messages.length === 0">暂无消息</div>
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
import { getMessages, markMessageRead } from '@/api/user'

const messages = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const loadError = ref('')

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await getMessages({ page: page.value, pageSize: pageSize.value })
    const d = res.data.data || res.data
    messages.value = d.list || []
    total.value = d.total || 0
  } catch (e) { loadError.value = '加载消息失败，请稍后重试'; console.error('加载消息失败', e) }
  finally { loading.value = false }
}

function handlePage(p) { page.value = p; fetchData() }

async function handleRead(m) {
  if (!m.isRead) {
    try {
      await markMessageRead(m.id)
      m.isRead = 1
    } catch (e) { console.error('标记已读失败', e) }
  }
}
</script>

<style scoped>
.message-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.message-list { display: flex; flex-direction: column; gap: 8px; }
.message-card { padding: 16px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); cursor: pointer; transition: background 0.2s; }
.message-card.unread { background: #f0f7ff; border-left: 3px solid var(--primary); }
.message-card:hover { background: #f5f5f5; }
.msg-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.msg-title { font-size: 15px; font-weight: 600; }
.msg-dot { width: 8px; height: 8px; background: #e53935; border-radius: 50%; flex-shrink: 0; }
.msg-content { font-size: 13px; color: var(--text-light); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 12px; color: var(--text-muted); }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
