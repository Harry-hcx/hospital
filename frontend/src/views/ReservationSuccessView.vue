<template>
  <div class="success-page">
    <AppHeader />
    <div class="page-content">
      <div class="success-card">
        <div class="success-icon" :class="{ pending: !paid }">{{ paid ? '✓' : '!' }}</div>
        <h2>{{ paid ? '预约成功' : expired ? '订单已过期' : '订单待支付' }}</h2>
        <div class="order-info" v-if="order.orderNo">
          <p>订单编号：{{ order.orderNo }}</p>
          <p>医生：{{ order.doctor }}</p>
          <p>医院：{{ order.hospital }}</p>
          <p>就诊时间：{{ order.date }} {{ order.timeSlot }}</p>
          <p>就诊人：{{ order.patientName }}</p>
          <p class="fee">费用：¥{{ order.fee }}</p>
        </div>
        <p v-if="!paid" class="tip">{{ expired ? '就诊开始时间已过，订单已自动关闭。' : '当前订单尚未支付完成，请返回支付页继续支付。' }}</p>
        <div class="actions">
          <router-link :to="paid || expired ? '/my-appointments' : `/reservation/pay/${route.params.orderNo}`" class="btn-primary">{{ paid || expired ? '查看订单' : '继续支付' }}</router-link>
          <router-link to="/" class="btn-home">返回首页</router-link>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getAppointmentSuccess } from '@/api/appointment'

const route = useRoute()
const order = ref({})
const paid = computed(() => Number(order.value?.status) === 2)
const expired = computed(() => Number(order.value?.status) === 6)

onMounted(async () => {
  try {
    const res = await getAppointmentSuccess(route.params.orderNo)
    order.value = res?.data || {}
  } catch (e) {
    console.error('加载预约结果失败', e)
  }
})
</script>

<style scoped>
.success-page { min-height: 100vh; background: var(--bg); }
.success-card { max-width: 500px; margin: 60px auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 40px; text-align: center; }
.success-icon { width: 72px; height: 72px; margin: 0 auto 20px; background: #4caf50; color: #fff; border-radius: 50%; font-size: 36px; display: flex; align-items: center; justify-content: center; }
.success-icon.pending { background: #faad14; }
.success-card h2 { font-size: 24px; margin-bottom: 20px; color: #4caf50; }
.order-info { text-align: left; background: var(--bg); border-radius: var(--radius); padding: 16px; margin-bottom: 24px; }
.order-info p { font-size: 14px; padding: 6px 0; border-bottom: 1px solid var(--border); }
.order-info p:last-child { border-bottom: none; }
.fee { color: #e53935; font-weight: 600; font-size: 16px !important; }
.tip { color: #ad6800; margin-bottom: 16px; }
.actions { display: flex; gap: 12px; justify-content: center; }
.btn-home { padding: 10px 24px; background: #fff; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; color: var(--text); }
</style>
