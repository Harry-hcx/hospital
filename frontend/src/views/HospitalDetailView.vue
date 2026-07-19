<template>
  <div class="hospital-detail-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <router-link to="/hospitals">找医院</router-link> > <span>{{ hospital.name }}</span>
      </div>

      <div class="detail-header" v-if="hospital.id">
        <img :src="resolveImageUrl(hospital.image, 'hospital_100001977.jpg')" :alt="hospital.name" class="header-img" />
        <div class="header-info">
          <h2>{{ hospital.name }} <span class="level-tag">{{ hospital.level }}</span></h2>
          <p class="addr">{{ hospital.province }}{{ hospital.city }}{{ hospital.district }} {{ hospital.address }}</p>
          <p class="phone" v-if="hospital.phone">电话：{{ hospital.phone }}</p>
          <p class="desc">{{ hospital.intro || hospital.description || '暂无简介' }}</p>
          <button class="btn-follow" @click="toggleFollow">
            {{ isFollowed ? '已关注' : '+ 关注' }}
          </button>
        </div>
      </div>

      <div class="detail-body">
        <!-- 科室 -->
        <div class="sidebar">
          <h3>科室列表</h3>
          <ul class="dept-list">
            <li v-for="d in departments" :key="d.id" :class="{ active: activeDeptId === d.id }" @click="activeDeptId = d.id">
              {{ d.name }}
            </li>
          </ul>
        </div>

        <!-- 医生列表 -->
        <div class="main">
          <h3>医生团队</h3>
          <div class="doctor-list">
            <div class="doctor-card" v-for="d in doctors" :key="d.id" @click="$router.push(`/doctor/${d.id}`)">
              <img :src="resolveImageUrl(d.avatar, 'doctor-male-doc.jpg') || defaultImg" :alt="d.name" class="doctor-img" />
              <div class="doctor-info">
                <h4>{{ d.name }} <span class="title-tag">{{ d.title }}</span></h4>
                <p>{{ d.departmentName }}</p>
              <p class="desc">{{ d.description || '暂无简介' }}</p>
                <RateStar :modelValue="d.rating || 0" readonly :size="'14px'" showText />
              </div>
            </div>
          </div>
          <div class="empty" v-if="!loadingDoctors && doctors.length === 0">暂无医生</div>
          <Pagination :total="doctorTotal" :current="doctorPage" :pageSize="doctorPageSize" @change="handleDoctorPage" />
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import RateStar from '@/components/RateStar.vue'
import Pagination from '@/components/Pagination.vue'
import { getHospitalDetail, getHospitalDepartments, getHospitalDoctors } from '@/api/hospital'
import { createFollow, deleteFollow, getMyFollows } from '@/api/user'
import { resolveImageUrl } from '@/utils/asset'

const route = useRoute()
const router = useRouter()

const hospital = ref({})
const departments = ref([])
const doctors = ref([])
const activeDeptId = ref(null)
const doctorTotal = ref(0)
const doctorPage = ref(1)
const doctorPageSize = ref(10)
const loadingDoctors = ref(false)
const isFollowed = ref(false)
const defaultImg = resolveImageUrl('doctor-male-doc.jpg', 'doctor-male-doc.jpg')

onMounted(async () => {
  const id = route.params.id
  try {
    const [hRes, dRes] = await Promise.all([
      getHospitalDetail(id),
      getHospitalDepartments(id)
    ])
    const hd = hRes.data.data || hRes.data
    hospital.value = hd
    const dd = dRes.data.data || dRes.data
    departments.value = dd || []
    if (departments.value.length) activeDeptId.value = departments.value[0].id
    if (localStorage.getItem('token')) {
      const fRes = await getMyFollows({ type: 1, page: 1, pageSize: 1000 })
      const fd = fRes.data.data || fRes.data
      isFollowed.value = (fd.list || []).some(item => Number(item.followId) === Number(id))
    }
  } catch (e) {
    console.error('加载医院详情失败', e)
  }
  if (!departments.value.length) fetchDoctors()
})

watch(activeDeptId, () => {
  doctorPage.value = 1
  fetchDoctors()
})

async function fetchDoctors() {
  loadingDoctors.value = true
  try {
    const res = await getHospitalDoctors(route.params.id, {
      page: doctorPage.value,
      pageSize: doctorPageSize.value,
      departmentId: activeDeptId.value || undefined,
    })
    const d = res.data.data || res.data
    doctors.value = d.list || []
    doctorTotal.value = d.total || 0
  } catch (e) {
    console.error('加载医生列表失败', e)
  } finally {
    loadingDoctors.value = false
  }
}

function handleDoctorPage(p) {
  doctorPage.value = p
  fetchDoctors()
}

async function toggleFollow() {
  try {
    if (isFollowed.value) {
      await deleteFollow(1, hospital.value.id)
      isFollowed.value = false
    } else {
      await createFollow(1, hospital.value.id)
      isFollowed.value = true
    }
  } catch (e) {
    console.error('关注操作失败', e)
  }
}
</script>

<style scoped>
.hospital-detail-page {
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

.detail-header {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-bottom: 24px;
}

.header-img {
  width: 240px;
  height: 180px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.header-info h2 {
  font-size: 24px;
}

.level-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #e8f5e9;
  color: #388e3c;
  border-radius: 3px;
  font-weight: normal;
  margin-left: 8px;
}

.addr, .phone {
  font-size: 14px;
  color: var(--text-light);
}

.desc {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.6;
}

.btn-follow {
  align-self: flex-start;
  padding: 8px 24px;
  background: var(--primary);
  color: #fff;
  border-radius: 4px;
  font-size: 14px;
  transition: background 0.2s;
}

.btn-follow:hover {
  background: var(--primary-dark);
}

.detail-body {
  display: flex;
  gap: 24px;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 16px;
}

.sidebar h3 {
  font-size: 16px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}

.dept-list li {
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text);
  transition: all 0.2s;
}

.dept-list li:hover {
  background: #e3f2fd;
  color: var(--primary);
}

.dept-list li.active {
  background: var(--primary);
  color: #fff;
}

.main {
  flex: 1;
}

.main h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.doctor-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s;
}

.doctor-card:hover {
  transform: translateX(4px);
}

.doctor-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  flex-shrink: 0;
}

.doctor-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.doctor-info h4 {
  font-size: 16px;
}

.title-tag {
  font-size: 12px;
  padding: 1px 6px;
  background: #e3f2fd;
  color: var(--primary);
  border-radius: 3px;
  font-weight: normal;
  margin-left: 6px;
}

.doctor-info p {
  font-size: 13px;
  color: var(--text-light);
}

.doctor-info .desc {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
  }
  .header-img {
    width: 100%;
    height: 200px;
  }
  .detail-body {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
}
</style>
