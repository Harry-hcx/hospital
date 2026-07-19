<template>
  <div class="doctor-list-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>找医生</span>
      </div>

      <div class="filter-bar">
        <div class="filter-item">
          <label>科室：</label>
          <select v-model="filters.departmentId" @change="handleSearch">
            <option value="">全部科室</option>
            <option v-for="d in departmentOptions" :key="d.id" :value="d.id">{{ d.parentName }} - {{ d.name }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label>关键词：</label>
          <input type="text" v-model="filters.keyword" placeholder="搜索医生姓名" @keyup.enter="handleSearch" />
        </div>
        <button class="btn-primary" @click="handleSearch">搜索</button>
      </div>

      <div class="doctor-grid">
        <div class="doctor-card" v-for="d in doctors" :key="d.id" @click="$router.push(`/doctor/${d.id}`)">
          <img :src="resolveDoctorImage(d)" :alt="d.name" class="doctor-img" />
          <div class="doctor-info">
            <h4>{{ d.name }} <span class="title-tag">{{ d.title }}</span></h4>
            <p class="dept">{{ d.hospitalName }} · {{ d.departmentName }}</p>
            <p class="desc">{{ d.description || '暂无简介' }}</p>
            <RateStar :modelValue="d.rating || 0" readonly :size="'14px'" showText />
          </div>
        </div>
      </div>

      <div class="empty" v-if="!loading && doctors.length === 0">暂无医生数据</div>
      <div class="error" v-if="!loading && error">{{ error }}</div>
      <div class="loading" v-if="loading">加载中...</div>

      <Pagination :total="total" :current="page" :pageSize="pageSize" @change="handlePageChange" />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import RateStar from '@/components/RateStar.vue'
import Pagination from '@/components/Pagination.vue'
import { getDoctors } from '@/api/doctor'
import { getDepartmentTree } from '@/api/department'
import { resolveImageUrl } from '@/utils/asset'

const doctors = ref([])
const departments = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const error = ref('')

const resolveDoctorImage = (doctor) => resolveImageUrl(doctor?.avatar, 'doctor-male-doc.jpg')

const filters = ref({ departmentId: '', keyword: '' })
const departmentOptions = computed(() => departments.value.flatMap(parent => {
  const children = Array.isArray(parent.children) ? parent.children : []
  return children.length
    ? children.map(child => ({ ...child, parentName: parent.name }))
    : [{ ...parent, parentName: '科室' }]
}))

onMounted(async () => {
  try {
    const res = await getDepartmentTree()
    departments.value = res?.data || []
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDoctors({ page: page.value, pageSize: pageSize.value, ...filters.value })
    const d = res?.data || {}
    doctors.value = d.records || []
    total.value = d.total || 0
  } catch (e) {
    error.value = '医生列表加载失败，请稍后重试'
    console.error('加载医生列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }
function handlePageChange(p) { page.value = p; fetchData() }
</script>

<style scoped>
.doctor-list-page { min-height: 100vh; background: var(--bg); }
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
.doctor-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
}
.doctor-card {
  padding: 20px; background: var(--bg-white); border-radius: var(--radius);
  box-shadow: var(--shadow); cursor: pointer; text-align: center;
  transition: transform 0.2s;
}
.doctor-card:hover { transform: translateY(-4px); }
.doctor-img { width: 90px; height: 90px; border-radius: 50%; object-fit: cover; margin-bottom: 12px; }
.doctor-info h4 { font-size: 16px; margin-bottom: 6px; }
.title-tag { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; font-weight: normal; margin-left: 4px; }
.dept { font-size: 13px; color: var(--text-light); margin-bottom: 4px; }
.desc { font-size: 13px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.empty, .loading, .error { text-align: center; padding: 60px; color: var(--text-muted); }
@media (max-width: 800px) { .doctor-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .doctor-grid { grid-template-columns: 1fr; } }
</style>
