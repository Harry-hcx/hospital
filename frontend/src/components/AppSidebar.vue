<template>
  <aside class="sidebar">
    <div class="menu-group">
      <div class="menu-title">订单中心</div>
      <ul>
        <li :class="{ active: activeMenu === 'myAppointment' }">
          <router-link to="/my-appointments">我的挂号</router-link>
        </li>
        <li :class="{ active: activeMenu === 'myConsult' }">
          <router-link to="/my-consults">我的咨询</router-link>
        </li>
        <li :class="{ active: activeMenu === 'myReviews' }">
          <router-link to="/my-reviews">我的评价</router-link>
        </li>
      </ul>
    </div>
    <div class="menu-group">
      <div class="menu-title">关注中心</div>
      <ul>
        <li :class="{ active: activeMenu === 'followHospital' }">
          <router-link to="/follow?type=1">关注的医院</router-link>
        </li>
        <li :class="{ active: activeMenu === 'followDoctor' }">
          <router-link to="/follow?type=2">关注的医生</router-link>
        </li>
        <li :class="{ active: activeMenu === 'followDisease' }">
          <router-link to="/follow?type=3">关注的疾病</router-link>
        </li>
      </ul>
    </div>
    <div class="menu-group">
      <div class="menu-title">账户中心</div>
      <ul>
        <li :class="{ active: activeMenu === 'profile' }">
          <router-link to="/profile">个人资料</router-link>
        </li>
        <li :class="{ active: activeMenu === 'familyMembers' }">
          <router-link to="/family-members">就诊成员</router-link>
        </li>
        <li :class="{ active: activeMenu === 'changePassword' }">
          <router-link to="/change-password">密码修改</router-link>
        </li>
      </ul>
    </div>
    <div class="menu-group">
      <div class="menu-title">消息中心</div>
      <ul>
        <li :class="{ active: activeMenu === 'messages' }">
          <router-link to="/messages">我的消息</router-link>
        </li>
        <li :class="{ active: activeMenu === 'feedback' }">
          <router-link to="/feedback">我的反馈</router-link>
        </li>
      </ul>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const menuMap = {
  '/my-appointments': 'myAppointment',
  '/my-consults': 'myConsult',
  '/my-reviews': 'myReviews',
  '/follow': 'followHospital',
  '/profile': 'profile',
  '/family-members': 'familyMembers',
  '/change-password': 'changePassword',
  '/messages': 'messages',
  '/feedback': 'feedback',
}

const activeMenu = computed(() => menuMap[route.path] || '')
</script>

<style scoped>
.sidebar {
  width: 220px;
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  flex-shrink: 0;
}

.menu-group {
  padding: 8px 0;
}

.menu-group + .menu-group {
  border-top: 1px solid var(--border);
}

.menu-title {
  padding: 10px 20px 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.menu-group ul li a {
  display: block;
  padding: 10px 20px 10px 28px;
  font-size: 14px;
  color: var(--text);
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.menu-group ul li a:hover {
  background: var(--bg);
  color: var(--primary);
}

.menu-group ul li.active a {
  background: rgba(30, 136, 229, 0.06);
  color: var(--primary);
  border-left-color: var(--primary);
  font-weight: 500;
}
</style>
