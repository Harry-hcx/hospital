import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppSidebar from '@/components/AppSidebar.vue'

describe('AppSidebar', () => {
  it('渲染四个菜单组', () => {
    const wrapper = mount(AppSidebar, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('订单中心')
    expect(wrapper.text()).toContain('关注中心')
    expect(wrapper.text()).toContain('账户中心')
    expect(wrapper.text()).toContain('消息中心')
  })

  it('渲染11个菜单项', () => {
    const wrapper = mount(AppSidebar, {
      global: { stubs: { 'router-link': { template: '<a><slot /></a>' } } },
    })
    const links = wrapper.findAll('a')
    const texts = links.map(l => l.text())
    expect(texts).toContain('我的挂号')
    expect(texts).toContain('我的咨询')
    expect(texts).toContain('我的评价')
    expect(texts).toContain('关注的医院')
    expect(texts).toContain('关注的医生')
    expect(texts).toContain('关注的疾病')
    expect(texts).toContain('个人资料')
    expect(texts).toContain('就诊成员')
    expect(texts).toContain('密码修改')
    expect(texts).toContain('我的消息')
    expect(texts).toContain('我的反馈')
  })
})
