<template>
  <div class="success-page">
    <AppHeader />
    <div class="page-content">
      <div class="success-card">
        <div class="success-icon" :class="{ pending: !paid }">{{ paid ? '✓' : '!' }}</div>
        <h2>{{ paid ? '咨询提交成功' : expired ? '订单已过期' : '订单待支付' }}</h2>
        <div class="order-info" v-if="order.orderNo">
          <p>订单编号：{{ order.orderNo }}</p>
          <p>医生：{{ order.doctor }}</p>
          <p>就诊人：{{ order.patientName }}</p>
          <p class="fee">费用：¥{{ order.fee }}</p>
        </div>
        <p class="tip">{{ paid ? '医生将在24小时内回复您的咨询，请留意消息通知' : expired ? '预约咨询开始时间已过，订单已自动关闭。' : '当前订单尚未支付完成，请返回支付页继续支付。' }}</p>
        <div class="actions">
          <router-link :to="paid || expired ? '/my-consults' : `/consult/pay/${route.params.orderNo}`" class="btn-primary">{{ paid || expired ? '查看咨询' : '继续支付' }}</router-link>
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
import { getConsultSuccess } from '@/api/consult'

const route = useRoute()
const order = ref({})
const paid = computed(() => Number(order.value?.status) === 2)
const expired = computed(() => Number(order.value?.status) === 6)

onMounted(async () => {
  try {
    const res = await getConsultSuccess(route.params.orderNo)
    order.value = res?.data || {}
  } catch (e) {
    console.error('加载咨询结果失败', e)
  }
})
</script>

<style scoped>
.success-page { min-height: 100vh; background: var(--bg); }
.success-card { max-width: 500px; margin: 60px auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 40px; text-align: center; }
.success-icon { width: 72px; height: 72px; margin: 0 auto 20px; background: #4caf50; color: #fff; border-radius: 50%; font-size: 36px; display: flex; align-items: center; justify-content: center; }
.success-icon.pending { background: #faad14; }
.success-card h2 { font-size: 24px; margin-bottom: 20px; color: #4caf50; }
.tip { font-size: 13px; color: var(--text-light); margin-bottom: 16px; }
.order-info { text-align: left; background: var(--bg); border-radius: var(--radius); padding: 16px; margin-bottom: 16px; }
.order-info p { font-size: 14px; padding: 6px 0; border-bottom: 1px solid var(--border); }
.order-info p:last-child { border-bottom: none; }
.fee { color: #e53935; font-weight: 600; font-size: 16px !important; }
.actions { display: flex; gap: 12px; justify-content: center; }
.btn-home { padding: 10px 24px; background: #fff; border: 1px solid var(--border); border-radius: 4px; font-size: 14px; color: var(--text); }
</style>
