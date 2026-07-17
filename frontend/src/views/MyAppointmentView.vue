<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-body">
      <AppSidebar />
      <div class="main-content">
        <h2>我的挂号 - 由成员4开发</h2>
        <p>此页面由成员4开发</p>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.page-body {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  display: flex;
  gap: 20px;
  flex: 1;
  width: 100%;
}
.main-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  padding: 30px;
}
</style>
