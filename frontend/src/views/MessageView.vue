<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main-content">
        <h2 class="page-title">消息通知</h2>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
        <div v-else class="msg-list">
          <div v-for="item in messages" :key="item.id" class="msg-card" :class="{ unread: item.isRead === 0 }" @click="readMsg(item)">
            <div class="msg-dot" v-if="item.isRead === 0"></div>
            <div class="msg-body">
              <h4>{{ item.title }}</h4>
              <p>{{ item.content }}</p>
              <span class="msg-time">{{ item.createTime }}</span>
            </div>
          </div>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <button :disabled="page <= 1" @click="page--; fetchMessages()">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="page++; fetchMessages()">下一页</button>
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
import { getMessagesApi, markReadApi } from '@/api/message'

const loading = ref(true)
const messages = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(() => fetchMessages())

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessagesApi({ page: page.value, pageSize: pageSize.value })
    if (res.data) {
      messages.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch { messages.value = [] }
  finally { loading.value = false }
}

async function readMsg(item) {
  if (item.isRead === 1) return
  try {
    await markReadApi(item.id)
    item.isRead = 1
  } catch { /* */ }
}
</script>

<style scoped>
.page-wrapper { min-height: 100vh; display: flex; flex-direction: column; }
.page-body { max-width: 1200px; margin: 20px auto; padding: 0 20px; display: flex; gap: 20px; flex: 1; width: 100%; }
.main-content { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); padding: 30px; }
.page-title { font-size: 20px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 2px solid #1e88e5; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }

.msg-list { display: flex; flex-direction: column; gap: 8px; }
.msg-card { display: flex; align-items: flex-start; gap: 12px; background: #f5f7fa; border-radius: 8px; padding: 16px 20px; cursor: pointer; transition: background 0.2s; }
.msg-card:hover { background: #eef0f4; }
.msg-card.unread { background: #e3f2fd; }
.msg-card.unread:hover { background: #bbdefb; }
.msg-dot { width: 8px; height: 8px; border-radius: 50%; background: #ff5252; margin-top: 6px; flex-shrink: 0; }
.msg-body { flex: 1; }
.msg-body h4 { font-size: 15px; margin-bottom: 4px; }
.msg-body p { font-size: 13px; color: #666; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.msg-time { font-size: 12px; color: #999; margin-top: 4px; display: block; }

.pagination { display: flex; gap: 12px; align-items: center; justify-content: center; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #e8e8e8; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
