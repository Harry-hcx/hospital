<template>
  <div class="home-page">
    <AppHeader />
    <div class="page-content">
      <!-- 轮播 Banner -->
      <div class="banner">
        <div class="banner-slide" :style="{ backgroundImage: `url(${currentBanner})` }">
          <div class="banner-text">
            <h2>健康之路，为您护航</h2>
            <p>汇聚优质医疗资源，让就医更简单</p>
          </div>
        </div>
        <div class="banner-dots">
          <span v-for="(b, i) in banners" :key="i" :class="{ active: i === bannerIndex }" @click="bannerIndex = i"></span>
        </div>
      </div>

      <!-- 热门医院 -->
      <section class="section">
        <div class="section-header">
          <h3>热门医院</h3>
          <router-link to="/hospitals" class="more">查看更多 →</router-link>
        </div>
        <div class="card-grid" v-if="hotHospitals && hotHospitals.length">
          <div class="card" v-for="h in hotHospitals" :key="h.id" @click="$router.push(`/hospital/${h.id}`)">
            <img :src="resolveHospitalImage(h)" :alt="h.name" class="card-img" />
            <div class="card-body">
              <h4>{{ h.name }}</h4>
              <p class="card-level">{{ h.level || '三级甲等' }}</p>
              <p class="card-addr">{{ h.province }}{{ h.city }}</p>
            </div>
          </div>
        </div>
        <div class="empty" v-else>暂无数据</div>
      </section>

      <!-- 热门医生 -->
      <section class="section">
        <div class="section-header">
          <h3>推荐医生</h3>
          <router-link to="/doctors" class="more">查看更多 →</router-link>
        </div>
        <div class="card-grid" v-if="hotDoctors && hotDoctors.length">
          <div class="card card-doctor" v-for="d in hotDoctors" :key="d.id" @click="$router.push(`/doctor/${d.id}`)">
            <img :src="resolveDoctorImage(d)" :alt="d.name" class="card-img" />
            <div class="card-body">
              <h4>{{ d.name }} <span class="title-tag">{{ d.title || '主治医师' }}</span></h4>
              <p>{{ d.hospitalName }} · {{ d.departmentName }}</p>
              <RateStar :modelValue="d.avgRating || 4.5" readonly :size="'14px'" showText />
            </div>
          </div>
        </div>
        <div class="empty" v-else>暂无数据</div>
      </section>

      <!-- 热门疾病 -->
      <section class="section">
        <div class="section-header">
          <h3>常见疾病</h3>
          <router-link to="/diseases" class="more">查看更多 →</router-link>
        </div>
        <div class="tag-list" v-if="hotDiseases && hotDiseases.length">
          <router-link v-for="d in hotDiseases" :key="d.id" :to="`/disease/${d.id}`" class="tag-item">
            {{ d.name }}
          </router-link>
        </div>
        <div class="empty" v-else>暂无数据</div>
      </section>

      <!-- 健康科普 -->
      <section class="section">
        <div class="section-header">
          <h3>健康科普</h3>
          <router-link to="/articles" class="more">查看更多 →</router-link>
        </div>
        <div class="article-list" v-if="hotArticles && hotArticles.length">
          <div class="article-item" v-for="a in hotArticles" :key="a.id" @click="$router.push(`/article/${a.id}`)">
            <img :src="resolveArticleImage(a)" :alt="a.title" class="article-img" />
            <div class="article-body">
              <h4>{{ a.title }}</h4>
              <p class="article-desc">{{ a.summary || '' }}</p>
              <span class="article-date">{{ a.createTime || '' }}</span>
            </div>
          </div>
        </div>
        <div class="empty" v-else>暂无数据</div>
      </section>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import RateStar from '@/components/RateStar.vue'
import { getHomeIndex } from '@/api/home'
import { DEFAULT_IMAGES, resolveImageUrl } from '@/utils/asset'

const banners = ref([
  DEFAULT_IMAGES.banner,
  resolveImageUrl('banner_02.jpeg', 'banner_01.jpeg'),
  resolveImageUrl('banner_03.jpeg', 'banner_01.jpeg'),
])
const bannerIndex = ref(0)
const currentBanner = computed(() => banners.value[bannerIndex.value] || banners.value[0] || '')
let timer = null

const hotHospitals = ref([])
const hotDoctors = ref([])
const hotDiseases = ref([])
const hotArticles = ref([])

const resolveHospitalImage = (hospital) => resolveImageUrl(hospital?.image || hospital?.avatar, 'hospital_100001977.jpg')
const resolveDoctorImage = (doctor) => resolveImageUrl(doctor?.avatar, 'doctor-male-doc.jpg')
const resolveArticleImage = (article) => resolveImageUrl(article?.cover || article?.image, 'health_01.jpeg')

const startBannerTimer = () => {
  if (timer) clearInterval(timer)
  if (banners.value.length <= 1) return

  timer = setInterval(() => {
    bannerIndex.value = (bannerIndex.value + 1) % banners.value.length
  }, 4000)
}

onMounted(async () => {
  startBannerTimer()

  try {
    const res = await getHomeIndex()
    const d = res?.data || {}

    const bannerImages = (d.banners || [])
      .map((item) => resolveImageUrl(typeof item === 'string' ? item : item?.image, 'banner_01.jpeg'))
      .filter(Boolean)

    if (bannerImages.length) {
      banners.value = bannerImages
      bannerIndex.value = 0
      startBannerTimer()
    }

    hotHospitals.value = d.recommendHospitals || d.hotHospitals || []
    hotDoctors.value = d.recommendDoctors || d.hotDoctors || []
    hotDiseases.value = d.recommendDiseases || d.hotDiseases || []
    hotArticles.value = d.recommendArticles || d.hotArticles || []
  } catch (e) {
    console.error('加载首页数据失败', e)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg);
}

.banner {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 32px;
}

.banner-slide {
  height: 320px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-image 0.8s ease;
}

.banner-text {
  text-align: center;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0,0,0,0.4);
}

.banner-text h2 {
  font-size: 36px;
  margin-bottom: 12px;
}

.banner-text p {
  font-size: 18px;
  opacity: 0.9;
}

.banner-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.banner-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: background 0.3s;
}

.banner-dots span.active {
  background: #fff;
}

.section {
  margin-bottom: 36px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary);
}

.section-header h3 {
  font-size: 20px;
  color: var(--text);
}

.more {
  font-size: 14px;
  color: var(--primary);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.card {
  background: var(--bg-white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}

.card-img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.card-body {
  padding: 12px;
}

.card-body h4 {
  font-size: 15px;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-tag {
  font-size: 12px;
  padding: 1px 6px;
  background: #e3f2fd;
  color: var(--primary);
  border-radius: 3px;
  font-weight: normal;
}

.card-body p {
  color: var(--text-light);
  font-size: 13px;
  margin-bottom: 2px;
}

.card-level {
  color: var(--primary) !important;
  font-weight: 500;
}

.card-addr {
  font-size: 12px !important;
  color: var(--text-muted) !important;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  padding: 8px 20px;
  background: var(--bg-white);
  border: 1px solid var(--border);
  border-radius: 20px;
  color: var(--text);
  font-size: 14px;
  transition: all 0.2s;
}

.tag-item:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: #e3f2fd;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s;
}

.article-item:hover {
  transform: translateX(4px);
}

.article-img {
  width: 160px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.article-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-body h4 {
  font-size: 16px;
  color: var(--text);
}

.article-desc {
  font-size: 13px;
  color: var(--text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-date {
  font-size: 12px;
  color: var(--text-muted);
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
  font-size: 14px;
}

@media (max-width: 900px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .banner-slide {
    height: 200px;
  }
  .article-item {
    flex-direction: column;
  }
  .article-img {
    width: 100%;
    height: 160px;
  }
}
</style>