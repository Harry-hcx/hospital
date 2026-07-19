import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ProfileView from '@/views/ProfileView.vue'
import { getProfile, updateProfile } from '@/api/user'

vi.mock('@/api/user', () => ({
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
}))

describe('ProfileView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getProfile.mockResolvedValue({
      data: {
        realName: '张三',
        gender: 1,
        phone: '13800138000',
        email: 'test@example.com',
        birthday: '1990-01-01',
        avatar: '',
      },
    })
    updateProfile.mockResolvedValue({ code: 200 })
  })

  it('displays numeric gender and saves realName', async () => {
    const wrapper = mount(ProfileView, {
      global: {
        stubs: {
          AppHeader: true,
          AppSidebar: true,
          AppFooter: true,
        },
      },
    })
    await flushPromises()

    expect(wrapper.find('input[placeholder="请输入姓名"]').element.value).toBe('张三')
    expect(wrapper.find('select').element.value).toBe('1')
    await wrapper.find('.btn-primary').trigger('click')
    await flushPromises()

    expect(updateProfile).toHaveBeenCalledWith(expect.objectContaining({
      realName: '张三',
      gender: 1,
    }))
  })
})
