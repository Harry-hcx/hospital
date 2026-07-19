<template>
  <div class="disease-detail-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <router-link to="/diseases">疾病库</router-link> > <span>{{ disease.name }}</span>
      </div>

      <div class="detail-body" v-if="disease.id">
        <h1>{{ disease.name }}</h1>
        <p class="dept" v-if="disease.departmentName">所属科室：{{ disease.departmentName }}</p>
        <div class="section">
          <h3>疾病简介</h3>
          <p class="content">{{ disease.description || '暂无简介' }}</p>
        </div>
        <div class="section" v-if="disease.symptoms || disease.symptom">
          <h3>常见症状</h3>
          <p class="content">{{ disease.symptoms || disease.symptom }}</p>
        </div>
        <div class="section" v-if="disease.treatment">
          <h3>治疗方法</h3>
          <p class="content">{{ disease.treatment }}</p>
        </div>
        <div class="section" v-if="disease.prevention">
          <h3>预防措施</h3>
          <p class="content">{{ disease.prevention }}</p>
        </div>
        <div class="actions">
          <button class="btn-follow" @click="toggleFollow">{{ isFollowed ? '已关注' : '+ 关注该疾病' }}</button>
          <router-link :to="`/doctors?departmentId=${disease.departmentId}`" class="btn-primary">查看相关医生</router-link>
        </div>
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
import { getDiseaseDetail } from '@/api/disease'
import { createFollow, deleteFollow, getMyFollows } from '@/api/user'

const route = useRoute()
const disease = ref({})
const isFollowed = ref(false)

onMounted(async () => {
  try {
    const res = await getDiseaseDetail(route.params.id)
    disease.value = res.data.data || res.data
    if (localStorage.getItem('token')) {
      const fRes = await getMyFollows({ type: 3, page: 1, pageSize: 1000 })
      const fd = fRes.data.data || fRes.data
      isFollowed.value = (fd.records || []).some(item => Number(item.followId) === Number(route.params.id))
    }
  } catch (e) { console.error('加载疾病详情失败', e) }
})

async function toggleFollow() {
  try {
    if (isFollowed.value) {
      await deleteFollow(3, disease.value.id)
      isFollowed.value = false
    } else {
      await createFollow(3, disease.value.id)
      isFollowed.value = true
    }
  } catch (e) { console.error('关注操作失败', e) }
}
</script>

<style scoped>
.disease-detail-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.detail-body { background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.detail-body h1 { font-size: 28px; margin-bottom: 12px; }
.dept { font-size: 14px; color: var(--primary); margin-bottom: 24px; }
.section { margin-bottom: 24px; }
.section h3 { font-size: 18px; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid var(--border); color: var(--text); }
.content { font-size: 15px; line-height: 1.8; color: var(--text-light); white-space: pre-wrap; }
.actions { display: flex; gap: 12px; margin-top: 24px; flex-wrap: wrap; }
.btn-follow { padding: 10px 24px; background: #fff; border: 1px solid var(--primary); color: var(--primary); border-radius: 4px; font-size: 14px; }
.btn-follow:hover { background: #e3f2fd; }
.empty { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
