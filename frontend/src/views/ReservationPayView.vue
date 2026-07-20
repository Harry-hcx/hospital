<template>
  <div class="pay-page">
    <AppHeader />
    <div class="page-content">
      <div class="page-breadcrumb">
        <router-link to="/">首页</router-link> > <span>支付确认</span>
      </div>
      <div class="form-card">
        <h2>订单支付</h2>
        <p v-if="payHint" class="pay-hint">{{ payHint }}</p>
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
        <button class="btn-primary btn-submit" @click="handlePay" :disabled="paying || isExpired">
          {{ isExpired ? '订单已过期' : paying ? '支付中...' : `确认支付 ¥${order.fee || 0}` }}
        </button>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import { getAppointmentDetail, payAppointment } from '@/api/appointment'

const route = useRoute()
const router = useRouter()
const order = ref({})
const payMethod = ref('wechat')
const paying = ref(false)
const isExpired = computed(() => Number(order.value?.status) === 6)

const payHint = computed(() => {
  if (isExpired.value) return '就诊开始时间已过，订单已自动关闭。'
  if (route.query.payResult === 'cancelled') return '您已取消或中断支付宝支付，订单仍为待支付状态。'
  if (route.query.payResult === 'verify_failed') return '支付结果校验失败，请重新发起支付。'
  return ''
})

onMounted(async () => {
  try {
    const res = await getAppointmentDetail(route.params.orderNo)
    order.value = res?.data || {}
  } catch (e) {
    console.error('加载订单详情失败', e)
  }
})

async function handlePay() {
  paying.value = true
  try {
    if (!order.value.orderNo) {
      alert('订单信息未加载完成')
      return
    }
    const res = await payAppointment(route.params.orderNo, { payMethod: payMethod.value })
    const data = res?.data || {}
    if (data.payMethod === 'alipay' && data.formHtml) {
      document.open()
      document.write(data.formHtml)
      document.close()
      return
    }
    if (data.redirectUrl) {
      router.push(data.redirectUrl)
      return
    }
    alert('未获取到支付跳转信息')
  } catch (e) {
    console.error('支付失败', e)
    alert('支付失败，请重试')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page { min-height: 100vh; background: var(--bg); }
.page-breadcrumb { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.page-breadcrumb a { color: var(--primary); }
.form-card { max-width: 600px; margin: 0 auto; background: var(--bg-white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 32px; }
.form-card h2 { font-size: 22px; margin-bottom: 16px; }
.pay-hint { margin-bottom: 16px; padding: 12px 14px; border-radius: var(--radius); background: #fff4e5; color: #ad6800; font-size: 14px; }
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
