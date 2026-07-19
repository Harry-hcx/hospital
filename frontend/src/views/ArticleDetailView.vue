<template>
  <div class="article-detail-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <router-link to="/articles">健康科普</router-link> > <span>{{ article.title }}</span>
      </div>

      <div class="article-body" v-if="article.id">
        <h1>{{ article.title }}</h1>
        <div class="article-meta">
          <span v-if="article.departmentName">{{ article.departmentName }}</span>
          <span>{{ article.publishTime || article.createTime }}</span>
          <span v-if="article.viewCount">{{ article.viewCount }} 阅读</span>
        </div>
        <div class="article-cover" v-if="articleCover">
          <img :src="articleCover" :alt="article.title" />
        </div>
        <div class="article-content" v-html="article.content || '暂无内容'"></div>
      </div>
      <div class="empty" v-else>加载中...</div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getArticleDetail } from '@/api/article'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const article = ref({})
const articleCover = ref('')

onMounted(async () => {
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res?.data || {}
    articleCover.value = resolveImageUrl(article.value.cover || article.value.image, 'health_01.jpeg')
  } catch (e) { console.error('加载文章详情失败', e) }
})
</script>

<style scoped>
.article-detail-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.article-body { background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.article-body h1 { font-size: 28px; margin-bottom: 16px; text-align: center; }
.article-meta { display: flex; justify-content: center; gap: 20px; font-size: 13px; color: var(--text-muted); margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.article-cover { margin-bottom: 24px; }
.article-cover img { width: 100%; max-height: 400px; object-fit: cover; border-radius: var(--radius); }
.article-content { font-size: 15px; line-height: 1.9; color: var(--text); }
.article-content :deep(img) { max-width: 100%; border-radius: 4px; }
.article-content :deep(p) { margin-bottom: 14px; }
.article-content :deep(h2), .article-content :deep(h3) { margin: 20px 0 10px; }
.empty { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
