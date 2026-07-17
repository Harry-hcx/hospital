import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppFooter from '@/components/AppFooter.vue'

describe('AppFooter', () => {
  it('渲染四个 section', () => {
    const wrapper = mount(AppFooter)
    expect(wrapper.text()).toContain('关于我们')
    expect(wrapper.text()).toContain('帮助中心')
    expect(wrapper.text()).toContain('服务项目')
    expect(wrapper.text()).toContain('联系方式')
  })

  it('渲染版权信息', () => {
    const wrapper = mount(AppFooter)
    expect(wrapper.text()).toContain('健康之路在线医疗平台')
    expect(wrapper.text()).toContain('400-888-8888')
  })
})
