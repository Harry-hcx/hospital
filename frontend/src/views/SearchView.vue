<template>
  <div class="search-page">
    <AppHeader />
    <div class="page-content">
      <div class="search-header">
        <h2>搜索结果：<span class="keyword">{{ keyword }}</span></h2>
        <div class="search-tabs">
          <button v-for="tab in tabs" :key="tab.type" :class="{ active: activeTab === tab.type }" @click="switchTab(tab.type)">{{ tab.label }}</button>
        </div>
      </div>

      <!-- 医院 -->
      <div v-if="activeTab === 'hospital'" class="result-list">
        <div class="result-card" v-for="item in list" :key="item.id" @click="$router.push(`/hospital/${item.id}`)">
          <img :src="resolveHospitalImage(item)" :alt="item.name" class="result-img" />
          <div class="result-info">
            <h4>{{ item.name }} <span class="level-tag">{{ item.level }}</span></h4>
            <p>{{ item.province }}{{ item.city }}{{ item.address }}</p>
          </div>
        </div>
      </div>

      <!-- 医生 -->
      <div v-if="activeTab === 'doctor'" class="result-list">
        <div class="result-card" v-for="item in list" :key="item.id" @click="$router.push(`/doctor/${item.id}`)">
          <img :src="resolveDoctorImage(item)" :alt="item.name" class="result-img avatar-round" />
          <div class="result-info">
            <h4>{{ item.name }} <span class="title-tag">{{ item.title }}</span></h4>
            <p>{{ item.hospitalName }} · {{ item.departmentName }}</p>
            <RateStar :modelValue="item.avgRating || 4.5" readonly :size="'14px'" showText />
          </div>
        </div>
      </div>

      <!-- 疾病 -->
      <div v-if="activeTab === 'disease'" class="result-grid">
        <div class="result-card grid-card" v-for="item in list" :key="item.id" @click="$router.push(`/disease/${item.id}`)">
          <h4>{{ item.name }}</h4>
          <p>{{ item.departmentName }}</p>
        </div>
      </div>

      <!-- 文章 -->
      <div v-if="activeTab === 'article'" class="result-list">
        <div class="result-card" v-for="item in list" :key="item.id" @click="$router.push(`/article/${item.id}`)">
          <img :src="resolveArticleImage(item)" :alt="item.title" class="result-img" />
          <div class="result-info">
            <h4>{{ item.title }}</h4>
            <p class="summary">{{ item.summary || '' }}</p>
            <span class="meta">{{ item.createTime }}</span>
          </div>
        </div>
      </div>

      <div class="empty" v-if="!loading && list.length === 0">未找到相关结果</div>
      <div class="loading" v-if="loading">搜索中...</div>
      <Pagination :total="total" :current="page" :pageSize="pageSize" @change="handlePage" />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import RateStar from '@/components/RateStar.vue'
import Pagination from '@/components/Pagination.vue'
import { globalSearch } from '@/api/search'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const keyword = ref('')
const activeTab = ref('hospital')
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const resolveHospitalImage = (item) => resolveImageUrl(item?.image || item?.avatar, 'hospital_100001977.jpg')
const resolveDoctorImage = (item) => resolveImageUrl(item?.avatar, 'doctor-male-doc.jpg')
const resolveArticleImage = (item) => resolveImageUrl(item?.cover || item?.image, 'health_01.jpeg')

const tabs = [
  { type: 'hospital', label: '医院' },
  { type: 'doctor', label: '医生' },
  { type: 'disease', label: '疾病' },
  { type: 'article', label: '文章' }
]

onMounted(() => {
  keyword.value = route.query.keyword || ''
  fetchData()
})

watch(() => route.query.keyword, (val) => {
  keyword.value = val || ''
  page.value = 1
  fetchData()
})

function switchTab(type) {
  activeTab.value = type
  page.value = 1
  fetchData()
}

async function fetchData() {
  if (!keyword.value) return
  loading.value = true
  try {
    const res = await globalSearch({
      keyword: keyword.value,
      type: activeTab.value,
      page: page.value,
      pageSize: pageSize.value
    })
    const d = res?.data || {}
    list.value = d.records || []
    total.value = d.total || 0
  } catch (e) { console.error('搜索失败', e) }
  finally { loading.value = false }
}

function handlePage(p) { page.value = p; fetchData() }
</script>

<style scoped>
.search-page { min-height: 100vh; background: var(--bg); }
.search-header { margin-bottom: 20px; }
.search-header h2 { font-size: 20px; margin-bottom: 12px; }
.keyword { color: var(--primary); }
.search-tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.search-tabs button {
  padding: 8px 20px; background: var(--bg-white); border: 1px solid var(--border);
  border-radius: 4px; font-size: 14px; color: var(--text); cursor: pointer; transition: all 0.2s;
}
.search-tabs button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.result-list { display: flex; flex-direction: column; gap: 12px; }
.result-card {
  display: flex; gap: 16px; padding: 16px; background: var(--bg-white);
  border-radius: var(--radius); box-shadow: var(--shadow); cursor: pointer; transition: transform 0.2s;
}
.result-card:hover { transform: translateX(4px); }
.result-img { width: 120px; height: 90px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.avatar-round { border-radius: 50%; width: 80px; height: 80px; }
.result-info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.result-info h4 { font-size: 16px; }
.level-tag { font-size: 12px; padding: 1px 6px; background: #e8f5e9; color: #388e3c; border-radius: 3px; font-weight: normal; margin-left: 6px; }
.title-tag { font-size: 12px; padding: 1px 6px; background: #e3f2fd; color: var(--primary); border-radius: 3px; font-weight: normal; margin-left: 6px; }
.result-info p { font-size: 13px; color: var(--text-light); }
.summary { font-size: 13px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.meta { font-size: 12px; color: var(--text-muted); }
.result-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.grid-card { flex-direction: column; border-left: 4px solid var(--primary); }
.grid-card h4 { font-size: 16px; margin-bottom: 6px; }
.empty, .loading { text-align: center; padding: 60px; color: var(--text-muted); }
@media (max-width: 600px) { .result-grid { grid-template-columns: 1fr; } }
</style>