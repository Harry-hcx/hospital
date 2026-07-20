import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import RegisterView from '@/views/RegisterView.vue'
import { registerApi, sendCaptchaApi } from '@/api/auth'

vi.mock('@/api/auth', () => ({
  sendCaptchaApi: vi.fn().mockResolvedValue({ code: 200, data: { cooldownSeconds: 60 } }),
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

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('渲染注册表单', () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('用户注册')
    expect(wrapper.text()).toContain('用户名')
    expect(wrapper.text()).toContain('验证码')
    expect(wrapper.text()).toContain('获取验证码')
    expect(wrapper.text()).not.toContain('确认密码')
  })

  it('手机号格式错误阻止提交', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('test-user')
    await inputs[1].setValue('123')
    await wrapper.find('form').trigger('submit.prevent')
    expect(registerApi).not.toHaveBeenCalled()
  })

  it('可以发送验证码', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[1].setValue('13800001111')
    await wrapper.find('.captcha-btn').trigger('click')
    await flushPromises()
    expect(sendCaptchaApi).toHaveBeenCalledWith({ phone: '13800001111' })
  })

  it('完整注册流程', async () => {
    const wrapper = mount(RegisterView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('test-user')
    await inputs[1].setValue('13800001111')
    await inputs[2].setValue('123456')
    await inputs[3].setValue('123456')
    await wrapper.find('select').setValue('1')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(wrapper.exists()).toBe(true)
    expect(registerApi).toHaveBeenCalledWith({
      username: 'test-user',
      phone: '13800001111',
      password: '123456',
      realName: undefined,
      email: undefined,
      gender: 1,
    })
  })
})
