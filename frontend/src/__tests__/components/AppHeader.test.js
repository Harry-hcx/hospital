import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import AppHeader from '@/components/AppHeader.vue'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/api/auth', () => ({
  loginApi: vi.fn(),
  getMeApi: vi.fn(),
  logoutApi: vi.fn().mockResolvedValue({ code: 200 }),
}))

describe('AppHeader', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('未登录时显示登录和注册链接', () => {
    const wrapper = mount(AppHeader, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('注册')
  })

  it('已登录时显示用户名和退出', () => {
    localStorage.setItem('token', 'x')
    localStorage.setItem('userInfo', JSON.stringify({ phone: '13812345678' }))
    setActivePinia(createPinia())
    const auth = useAuthStore()

    const wrapper = mount(AppHeader, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })

    expect(wrapper.text()).toContain('退出')
    expect(wrapper.text()).toContain('138****5678')
  })

  it('点击退出按钮', async () => {
    localStorage.setItem('token', 'x')
    localStorage.setItem('userInfo', JSON.stringify({ phone: '138' }))
    setActivePinia(createPinia())
    const wrapper = mount(AppHeader, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })

    await wrapper.find('.logout-btn').trigger('click')
    await flushPromises()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('渲染导航链接', () => {
    const wrapper = mount(AppHeader, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const links = wrapper.findAll('a')
    const texts = links.map(l => l.text())
    expect(texts).toContain('首页')
    expect(texts).toContain('找医院')
    expect(texts).toContain('找医生')
    expect(texts).toContain('查疾病')
    expect(texts).toContain('健康科普')
  })
})
