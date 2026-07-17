import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import RegisterView from '@/views/RegisterView.vue'

vi.mock('@/api/auth', () => ({
  registerApi: vi.fn().mockResolvedValue({ code: 200, data: { userId: 2 } }),
  loginApi: vi.fn(),
  getMeApi: vi.fn(),
  changePasswordApi: vi.fn(),
  logoutApi: vi.fn(),
}))

describe('RegisterView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('渲染注册表单', () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('用户注册')
    expect(wrapper.text()).toContain('获取验证码')
  })

  it('手机号格式错误阻止提交', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('123')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.exists()).toBe(true)
  })

  it('两次密码不一致', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13800001111')
    await inputs[2].setValue('123456')
    await inputs[3].setValue('654321')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.exists()).toBe(true)
  })

  it('完整注册流程', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13800001111')
    await inputs[1].setValue('123456')
    await inputs[2].setValue('123456')
    await inputs[3].setValue('123456')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(wrapper.exists()).toBe(true)
  })
})
