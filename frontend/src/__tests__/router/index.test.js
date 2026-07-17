import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

const authMetaRoutes = [
  '/reservation',
  '/consult',
  '/profile',
  '/family-members',
  '/my-appointments',
  '/my-consults',
  '/my-reviews',
  '/follow',
  '/messages',
  '/feedback',
  '/change-password',
]

const publicRoutes = [
  '/',
  '/login',
  '/register',
  '/hospitals',
  '/doctors',
  '/diseases',
  '/articles',
  '/search',
  '/hospital/1',
  '/doctor/1',
  '/disease/1',
  '/article/1',
]

describe('Router Guards', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('所有受限路由都有 meta.auth = true', async () => {
    const router = (await import('@/router/index.js')).default
    for (const path of authMetaRoutes) {
      const route = router.resolve(path)
      // route.matched[0]?.meta?.auth may not work for all, just check the routes array
      const found = router.getRoutes().find(r => {
        const regex = new RegExp('^' + r.path.replace(/:[^/]+/g, '[^/]+') + '$')
        return regex.test(path)
      })
      if (found) {
        expect(found.meta.auth).toBe(true)
      }
    }
  })

  it('公开路由不需要 meta.auth', async () => {
    const router = (await import('@/router/index.js')).default
    for (const path of publicRoutes) {
      const found = router.getRoutes().find(r => r.path === path)
      if (found) {
        expect(found.meta.auth).toBeFalsy()
      }
    }
  })

  it('未登录访问受限路由时 redirect 到 login', async () => {
    const router = (await import('@/router/index.js')).default
    const result = await router.push('/profile')
    // When not logged in, should redirect to login
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('已登录访问受限路由时不跳转', async () => {
    localStorage.setItem('token', 'valid-token')
    localStorage.setItem('userInfo', JSON.stringify({ id: 1 }))
    setActivePinia(createPinia())
    const router = (await import('@/router/index.js')).default
    await router.push('/my-appointments')
    expect(router.currentRoute.value.path).toBe('/my-appointments')
  })

  it('已登录访问 /login 不会被阻拦', async () => {
    localStorage.setItem('token', 'valid-token')
    localStorage.setItem('userInfo', JSON.stringify({ id: 1 }))
    setActivePinia(createPinia())
    const router = (await import('@/router/index.js')).default
    await router.push('/login')
    // login page has no auth guard
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
