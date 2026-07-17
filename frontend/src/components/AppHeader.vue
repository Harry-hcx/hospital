<template>
  <header class="app-header">
    <div class="header-inner">
      <div class="logo">
        <router-link to="/">健康之路在线医疗</router-link>
      </div>
      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/hospitals">找医院</router-link>
        <router-link to="/doctors">找医生</router-link>
        <router-link to="/diseases">查疾病</router-link>
        <router-link to="/articles">健康科普</router-link>
      </nav>
      <div class="search-box">
        <input v-model="searchText" type="text" placeholder="搜索医院、医生、疾病..." @keyup.enter="handleSearch" />
        <button @click="handleSearch">搜索</button>
      </div>
      <div class="auth">
        <template v-if="auth.isLoggedIn">
          <router-link to="/my-appointments" class="user-info">你好, {{ auth.maskedPhone }}</router-link>
          <a class="logout-btn" @click="handleLogout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">登录</router-link>
          <router-link to="/register" class="register-btn">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const searchText = ref('')

function handleSearch() {
  if (searchText.value.trim()) {
    router.push(`/search?keyword=${encodeURIComponent(searchText.value)}`)
  }
}

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  height: var(--header-height);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 20px;
}

.logo a {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  white-space: nowrap;
}

.nav {
  display: flex;
  gap: 4px;
}

.nav a {
  color: rgba(255, 255, 255, 0.85);
  padding: 6px 14px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.search-box {
  display: flex;
  flex: 1;
  max-width: 360px;
}

.search-box input {
  flex: 1;
  border: none;
  border-radius: 4px 0 0 4px;
  padding: 7px 12px;
  font-size: 13px;
}

.search-box button {
  background: #fff;
  border: none;
  color: var(--primary);
  padding: 0 16px;
  border-radius: 0 4px 4px 0;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.auth {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
  margin-left: auto;
}

.login-btn,
.register-btn {
  color: #fff;
  font-size: 13px;
}

.login-btn:hover,
.register-btn:hover {
  text-decoration: underline;
}

.user-info {
  color: #fff;
  font-size: 13px;
}

.logout-btn {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  cursor: pointer;
}

.logout-btn:hover {
  color: #fff;
}
</style>
