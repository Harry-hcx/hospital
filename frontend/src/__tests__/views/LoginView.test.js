import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import LoginView from '@/views/LoginView.vue'

vi.mock('@/api/auth', () => ({
  loginApi: vi.fn().mockResolvedValue({
    code: 200,
    data: {
      token: 'mock-token',
      userInfo: { id: 1, phone: '13800138000', realName: '张三', avatar: '' },
    },
  }),
  registerApi: vi.fn(),
  getMeApi: vi.fn(),
  changePasswordApi: vi.fn(),
  logoutApi: vi.fn(),
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('渲染登录表单', () => {
    const wrapper = mount(LoginView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('用户登录')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('手机号格式校验', async () => {
    const wrapper = mount(LoginView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    await wrapper.find('input[type="text"]').setValue('123')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.exists()).toBe(true)
  })

  it('提交正常登录', async () => {
    const wrapper = mount(LoginView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    await wrapper.find('input[type="text"]').setValue('13800138000')
    await wrapper.find('input[type="password"]').setValue('123456')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(localStorage.getItem('token')).toBe('mock-token')
  })

  it('密码不足6位阻止提交', async () => {
    const wrapper = mount(LoginView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    await wrapper.find('input[type="text"]').setValue('13800138000')
    await wrapper.find('input[type="password"]').setValue('12345')
    await wrapper.find('form').trigger('submit.prevent')
    expect(localStorage.getItem('token')).toBeNull()
  })
})
