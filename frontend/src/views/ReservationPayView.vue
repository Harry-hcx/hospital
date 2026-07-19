<template>
  <div class="pay-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>支付确认</span>
      </div>
      <div class="form-card">
        <h2>订单支付</h2>
        <div class="order-info" v-if="order.orderNo">
          <div class="info-row"><span>订单编号</span><strong>{{ order.orderNo }}</strong></div>
          <div class="info-row"><span>医生</span><strong>{{ order.doctor }}</strong></div>
          <div class="info-row"><span>医院</span><strong>{{ order.hospital }}</strong></div>
          <div class="info-row"><span>就诊时间</span><strong>{{ order.date }} {{ order.timeSlot }}</strong></div>
          <div class="info-row"><span>就诊人</span><strong>{{ order.patientName }}</strong></div>
          <div class="info-row"><span>挂号费</span><strong class="fee">¥{{ order.fee }}</strong></div>
        </div>
        <div class="pay-methods">
          <label>支付方式</label>
          <div class="methods">
            <label class="method" :class="{ active: payMethod === 'wechat' }"><input type="radio" v-model="payMethod" value="wechat" /> 微信支付</label>
            <label class="method" :class="{ active: payMethod === 'alipay' }"><input type="radio" v-model="payMethod" value="alipay" /> 支付宝</label>
          </div>
        </div>
        <button class="btn-primary btn-submit" @click="handlePay" :disabled="paying">
          {{ paying ? '支付中...' : `确认支付 ¥${order.fee || 0}` }}
        </button>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getAppointmentDetail, payAppointment } from '@/api/appointment'

const route = useRoute()
const router = useRouter()
const order = ref({})
const payMethod = ref('wechat')
const paying = ref(false)
const loadError = ref('')

onMounted(async () => {
  try {
    const res = await getAppointmentDetail(route.params.orderNo)
    order.value = res?.data || {}
  } catch (e) { console.error('加载订单详情失败', e) }
})

async function handlePay() {
  paying.value = true
  try {
    if (!order.value.orderNo) { alert('订单信息未加载完成'); return }
    await payAppointment(route.params.orderNo, { payType: payMethod.value === 'wechat' ? 1 : 2 })
    router.push(`/reservation/success/${route.params.orderNo}`)
  } catch (e) {
    console.error('支付失败', e)
    alert('支付失败，请重试')
  } finally { paying.value = false }
}
</script>

<style scoped>
.pay-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.form-card { max-width: 600px; margin: 0 auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.form-card h2 { font-size: 22px; margin-bottom: 24px; }
.order-info { background: var(--bg); border-radius: var(--radius); padding: 16px; margin-bottom: 24px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; border-bottom: 1px solid var(--border); }
.info-row:last-child { border-bottom: none; }
.info-row span { color: var(--text-light); }
.fee { color: #e53935; font-size: 18px; }
.pay-methods { margin-bottom: 24px; }
.pay-methods label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 10px; }
.methods { display: flex; gap: 12px; }
.method { display: flex; align-items: center; gap: 6px; padding: 12px 20px; border: 2px solid var(--border); border-radius: var(--radius); cursor: pointer; font-size: 14px; }
.method.active { border-color: var(--primary); background: #e3f2fd; }
.btn-submit { width: 100%; padding: 14px; font-size: 16px; }
</style>
