<template>
  <div class="hospital-list-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>找医院</span>
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
          <label>等级：</label>
          <select v-model="filters.level" @change="handleSearch">
            <option value="">全部等级</option>
            <option value="三甲">三甲</option>
            <option value="三乙">三乙</option>
            <option value="二甲">二甲</option>
            <option value="二乙">二乙</option>
          </select>
        </div>
        <div class="filter-item">
          <label>关键词：</label>
          <input type="text" v-model="filters.keyword" placeholder="搜索医院名称" @keyup.enter="handleSearch" />
        </div>
        <button class="btn-primary" @click="handleSearch">搜索</button>
      </div>

      <div class="hospital-list">
        <div class="hospital-card" v-for="h in hospitals" :key="h.id" @click="$router.push(`/hospital/${h.id}`)">
          <img :src="resolveHospitalImage(h)" :alt="h.name" class="hospital-img" />
          <div class="hospital-info">
            <h3>{{ h.name }} <span class="level-tag">{{ h.level || '三甲' }}</span></h3>
            <p class="addr">{{ h.province }}{{ h.city }}{{ h.district || '' }} {{ h.address || '' }}</p>
            <p class="desc">{{ h.description || '暂无简介' }}</p>
            <div class="hospital-tags">
              <span v-if="h.departmentCount">科室数：{{ h.departmentCount }}</span>
              <span v-if="h.doctorCount">医生数：{{ h.doctorCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty" v-if="!loading && hospitals.length === 0">暂无医院数据</div>
      <div class="error" v-if="!loading && error">{{ error }}</div>
      <div class="loading" v-if="loading">加载中...</div>

      <Pagination
        :total="total"
        :current="page"
        :pageSize="pageSize"
        @change="handlePageChange"
      />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import Pagination from '@/components/Pagination.vue'
import { getHospitals } from '@/api/hospital'
import { getPrimaryDepartments } from '@/api/department'
import { resolveImageUrl } from '@/utils/asset'

const hospitals = ref([])
const departments = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const error = ref('')

const resolveHospitalImage = (hospital) => resolveImageUrl(hospital?.image || hospital?.avatar, 'hospital_100001977.jpg')

const filters = ref({
  departmentId: '',
  level: '',
  keyword: '',
  province: '',
  city: ''
})

onMounted(async () => {
  try {
    const res = await getPrimaryDepartments()
    const d = res?.data || []
    departments.value = d || []
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const res = await getHospitals({
      page: page.value,
      pageSize: pageSize.value,
      ...filters.value
    })
    const d = res?.data || {}
    hospitals.value = d.records || []
    total.value = d.total || 0
  } catch (e) {
    error.value = '医院列表加载失败，请稍后重试'
    console.error('加载医院列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handlePageChange(p) {
  page.value = p
  fetchData()
}
</script>

<style scoped>
.hospital-list-page {
  min-height: 100vh;
  background: var(--bg);
}

.page-breadcrumb {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.page-breadcrumb a {
  color: var(--primary);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 16px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-item label {
  font-size: 13px;
  color: var(--text-light);
  white-space: nowrap;
}

.filter-item select,
.filter-item input {
  padding: 6px 10px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 13px;
  min-width: 140px;
}

.hospital-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hospital-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s;
}

.hospital-card:hover {
  transform: translateX(4px);
}

.hospital-img {
  width: 200px;
  height: 140px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.hospital-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hospital-info h3 {
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #e8f5e9;
  color: #388e3c;
  border-radius: 3px;
  font-weight: normal;
}

.addr {
  font-size: 13px;
  color: var(--text-light);
}

.desc {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hospital-tags {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-muted);
}

.empty, .loading, .error {
  text-align: center;
  padding: 60px;
  color: var(--text-muted);
  font-size: 14px;
}

@media (max-width: 700px) {
  .hospital-card {
    flex-direction: column;
  }
  .hospital-img {
    width: 100%;
    height: 180px;
  }
}
</style>
