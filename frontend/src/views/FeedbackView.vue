<template>
  <div class="feedback-page">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main">
        <h2>意见反馈</h2>
        <div class="form-card">
          <div class="form-group">
            <label>反馈类型</label>
            <select v-model="form.type">
              <option value="">请选择</option>
              <option :value="1">功能异常</option>
              <option :value="2">建议</option>
              <option :value="3">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>反馈内容</label>
            <textarea v-model="form.content" rows="6" placeholder="请详细描述您遇到的问题或建议..."></textarea>
          </div>
          <button class="btn-primary" @click="handleSubmit" :disabled="submitting">{{ submitting ? '提交中...' : '提交反馈' }}</button>
        </div>

        <div class="history-section">
          <h3>我的反馈记录</h3>
          <div class="feedback-list">
            <div class="feedback-card" v-for="f in feedbacks" :key="f.id">
              <div class="fb-header">
                <span class="fb-type">{{ typeMap[f.type] || f.type }}</span>
                <span class="fb-date">{{ f.createTime }}</span>
              </div>
              <p class="fb-content">{{ f.content }}</p>
            </div>
          </div>
          <div class="empty" v-if="feedbacks.length === 0">暂无反馈记录</div>
        </div>
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
import { getMyFeedbacks, createFeedback } from '@/api/user'

const feedbacks = ref([])
const submitting = ref(false)
const loading = ref(false)
const loadError = ref('')
const typeMap = { 1: '功能异常', 2: '建议', 3: '其他' }
const form = ref({ type: '', content: '' })

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyFeedbacks({ page: 1, pageSize: 100 })
    const d = res.data.data || res.data
    feedbacks.value = d.records || []
  } catch (e) { loadError.value = '加载反馈记录失败，请稍后重试'; console.error('加载反馈记录失败', e) }
  finally { loading.value = false }
})

async function handleSubmit() {
  if (!form.value.content) { alert('请输入反馈内容'); return }
  submitting.value = true
  try {
    await createFeedback(form.value)
    alert('反馈提交成功')
    form.value = { type: '', content: '' }
    const res = await getMyFeedbacks({ page: 1, pageSize: 100 })
    const d = res.data.data || res.data
    feedbacks.value = d.records || []
  } catch (e) { console.error('提交失败', e); alert('提交失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.feedback-page { min-height: 100vh; background: var(--bg); }
.page-body { display: flex; gap: 24px; }
.main { flex: 1; }
.main h2 { font-size: 20px; margin-bottom: 16px; }
.form-card { background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 24px; margin-bottom: 32px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; }
.form-group textarea { resize: vertical; }
.history-section { margin-top: 32px; }
.history-section h3 { font-size: 18px; margin-bottom: 12px; }
.feedback-list { display: flex; flex-direction: column; gap: 10px; }
.feedback-card { padding: 14px; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); }
.fb-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.fb-type { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; }
.fb-date { font-size: 12px; color: var(--text-muted); }
.fb-content { font-size: 14px; color: var(--text); line-height: 1.5; }
.empty { text-align: center; padding: 30px; color: var(--text-muted); }
@media (max-width: 768px) { .page-body { flex-direction: column; } }
</style>
