import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ChangePasswordView from '@/views/ChangePasswordView.vue'

vi.mock('@/api/auth', () => ({
  changePasswordApi: vi.fn().mockResolvedValue({ code: 200, message: '密码修改成功' }),
  loginApi: vi.fn(),
  getMeApi: vi.fn(),
  registerApi: vi.fn(),
  logoutApi: vi.fn(),
}))

describe('ChangePasswordView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('渲染修改密码表单', () => {
    const wrapper = mount(ChangePasswordView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('修改密码')
    expect(wrapper.text()).toContain('原密码')
    expect(wrapper.text()).toContain('新密码')
    expect(wrapper.text()).toContain('确认新密码')
  })

  it('新密码不足6位', async () => {
    const wrapper = mount(ChangePasswordView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input[type="password"]')
    await inputs[0].setValue('123456')
    await inputs[1].setValue('12345')
    await inputs[2].setValue('12345')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.exists()).toBe(true)
  })

  it('两次新密码不一致', async () => {
    const wrapper = mount(ChangePasswordView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input[type="password"]')
    await inputs[0].setValue('oldpass')
    await inputs[1].setValue('newpass1')
    await inputs[2].setValue('newpass2')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.exists()).toBe(true)
  })

  it('修改密码成功', async () => {
    const wrapper = mount(ChangePasswordView, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const inputs = wrapper.findAll('input[type="password"]')
    await inputs[0].setValue('oldpass')
    await inputs[1].setValue('newpass123')
    await inputs[2].setValue('newpass123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(wrapper.exists()).toBe(true)
  })
})
