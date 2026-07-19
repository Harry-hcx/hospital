<template>
  <div class="article-list-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>健康科普</span>
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
          <input type="text" v-model="filters.keyword" placeholder="搜索文章标题" @keyup.enter="handleSearch" />
        </div>
        <button class="btn-primary" @click="handleSearch">搜索</button>
      </div>

      <div class="article-list">
        <div class="article-card" v-for="a in articles" :key="a.id" @click="$router.push(`/article/${a.id}`)">
          <img :src="resolveArticleImage(a)" :alt="a.title" class="article-img" />
          <div class="article-body">
            <h3>{{ a.title }}</h3>
            <p class="summary">{{ a.summary || '' }}</p>
            <div class="article-meta">
              <span v-if="a.departmentName">{{ a.departmentName }}</span>
              <span>{{ a.publishTime || a.createTime }}</span>
              <span v-if="a.viewCount">{{ a.viewCount }} 阅读</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty" v-if="!loading && articles.length === 0">暂无文章</div>
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
import { getArticles } from '@/api/article'
import { getPrimaryDepartments } from '@/api/department'
import { resolveImageUrl } from '@/utils/asset'

const articles = ref([])
const departments = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const error = ref('')

const resolveArticleImage = (article) => resolveImageUrl(article?.cover || article?.image, 'health_01.jpeg')

const filters = ref({ departmentId: '', keyword: '' })

onMounted(async () => {
  try {
    const res = await getPrimaryDepartments()
    departments.value = res?.data || []
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const res = await getArticles({ page: page.value, pageSize: pageSize.value, ...filters.value })
    const d = res?.data || {}
    articles.value = d.list || []
    total.value = d.total || 0
  } catch (e) {
    error.value = '文章列表加载失败，请稍后重试'
    console.error('加载文章列表失败', e)
  }
  finally { loading.value = false }
}

function handleSearch() { page.value = 1; fetchData() }
function handlePageChange(p) { page.value = p; fetchData() }
</script>

<style scoped>
.article-list-page { min-height: 100vh; background: var(--bg); }
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
.article-list { display: flex; flex-direction: column; gap: 16px; }
.article-card {
  display: flex; gap: 20px; padding: 20px; background: var(--bg-white);
  border-radius: var(--radius); box-shadow: var(--shadow); cursor: pointer;
  transition: transform 0.2s;
}
.article-card:hover { transform: translateX(4px); }
.article-img { width: 240px; height: 150px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.article-body { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.article-body h3 { font-size: 18px; color: var(--text); margin-bottom: 8px; }
.summary { font-size: 14px; color: var(--text-light); line-height: 1.6; flex: 1; }
.article-meta { display: flex; gap: 16px; font-size: 12px; color: var(--text-muted); margin-top: 10px; }
.empty, .loading, .error { text-align: center; padding: 60px; color: var(--text-muted); }
@media (max-width: 600px) {
  .article-card { flex-direction: column; }
  .article-img { width: 100%; height: 180px; }
}
</style>
