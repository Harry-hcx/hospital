<template>
  <div class="disease-list-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>疾病库</span>
      </div>

      <div class="filter-bar">
        <div class="filter-item">
          <label>科室：</label>
          <select v-model="filters.departmentId" @change="handleSearch">
            <option value="">全部科室</option>
            <option v-for="d in departments" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label>关键词：</label>
          <input type="text" v-model="filters.keyword" placeholder="搜索疾病名称" @keyup.enter="handleSearch" />
        </div>
        <button class="btn-primary" @click="handleSearch">搜索</button>
      </div>

      <div class="disease-grid">
        <div class="disease-card" v-for="d in diseases" :key="d.id" @click="$router.push(`/disease/${d.id}`)">
          <h4>{{ d.name }}</h4>
          <p class="dept">{{ d.departmentName }}</p>
          <p class="desc">{{ d.description || '暂无简介' }}</p>
        </div>
      </div>

      <div class="empty" v-if="!loading && diseases.length === 0">暂无疾病数据</div>
      <div class="error" v-if="!loading && error">{{ error }}</div>
      <div class="loading" v-if="loading">加载中...</div>

      <Pagination :total="total" :current="page" :pageSize="pageSize" @change="handlePageChange" />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import Pagination from '@/components/Pagination.vue'
import { getDiseases } from '@/api/disease'
import { getPrimaryDepartments } from '@/api/department'

const diseases = ref([])
const departments = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const error = ref('')

const filters = ref({ departmentId: '', keyword: '' })

onMounted(async () => {
  try {
    const res = await getPrimaryDepartments()
    departments.value = (res.data.data || res.data) || []
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDiseases({ page: page.value, pageSize: pageSize.value, ...filters.value })
    const d = res.data.data || res.data
    diseases.value = d.list || []
    total.value = d.total || 0
  } catch (e) {
    error.value = '疾病列表加载失败，请稍后重试'
    console.error('加载疾病列表失败', e)
  } finally { loading.value = false }
}

function handleSearch() { page.value = 1; fetchData() }
function handlePageChange(p) { page.value = p; fetchData() }
</script>

<style scoped>
.disease-list-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.filter-bar {
  display: flex; flex-wrap: wrap; gap: 12px; align-items: center;
  padding: 16px; background: var(--bg-white); border-radius: var(--radius);
  box-shadow: var(--shadow); margin-bottom: 20px;
}
.filter-item { display: flex; align-items: center; gap: 6px; }
.filter-item label { font-size: 13px; color: var(--text-light); white-space: nowrap; }
.filter-item select, .filter-item input { padding: 6px 10px; border: 1px solid var(--border); border-radius: 4px; font-size: 13px; min-width: 140px; }
.disease-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.disease-card {
  padding: 20px; background: var(--bg-white); border-radius: var(--radius);
  box-shadow: var(--shadow); cursor: pointer; transition: transform 0.2s;
  border-left: 4px solid var(--primary);
}
.disease-card:hover { transform: translateY(-3px); }
.disease-card h4 { font-size: 16px; color: var(--text); margin-bottom: 8px; }
.dept { font-size: 13px; color: var(--primary); margin-bottom: 6px; }
.desc { font-size: 13px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty, .loading, .error { text-align: center; padding: 60px; color: var(--text-muted); }
@media (max-width: 800px) { .disease-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .disease-grid { grid-template-columns: 1fr; } }
</style>
