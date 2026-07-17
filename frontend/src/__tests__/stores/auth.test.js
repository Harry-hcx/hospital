import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('AuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('初始状态：未登录', () => {
    const auth = useAuthStore()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.token).toBe('')
    expect(auth.userInfo).toBeNull()
  })

  it('从 localStorage 恢复 token', () => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('userInfo', JSON.stringify({ phone: '13800138000', realName: '张三' }))
    setActivePinia(createPinia())
    const auth = useAuthStore()
    expect(auth.isLoggedIn).toBe(true)
    expect(auth.userInfo.phone).toBe('13800138000')
  })

  it('maskedPhone 格式化手机号', () => {
    localStorage.setItem('token', 'x')
    localStorage.setItem('userInfo', JSON.stringify({ phone: '13812345678' }))
    setActivePinia(createPinia())
    const auth = useAuthStore()
    expect(auth.maskedPhone).toBe('138****5678')
  })

  it('logout 清除状态', () => {
    localStorage.setItem('token', 'x')
    localStorage.setItem('userInfo', JSON.stringify({ phone: '138' }))
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.logout()
    expect(auth.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
