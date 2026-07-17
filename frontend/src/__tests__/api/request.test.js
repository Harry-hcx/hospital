import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('Axios Request Module', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.resetModules()
  })

  it('模块可以正常导入', async () => {
    const request = (await import('@/api/request')).default
    expect(request).toBeDefined()
    expect(typeof request.get).toBe('function')
    expect(typeof request.post).toBe('function')
  })

  it('创建实例有 baseURL', async () => {
    const request = (await import('@/api/request')).default
    expect(request.defaults.baseURL).toBeDefined()
  })

  it('创建实例有 timeout', async () => {
    const request = (await import('@/api/request')).default
    expect(request.defaults.timeout).toBe(10000)
  })

  it('请求拦截器注入 Bearer Token', async () => {
    localStorage.setItem('token', 'test-jwt')
    const request = (await import('@/api/request')).default

    const config = { headers: {} }
    for (const h of request.interceptors.request.handlers) {
      if (h.fulfilled) {
        const result = await h.fulfilled(config)
        if (result?.headers?.Authorization) {
          expect(result.headers.Authorization).toBe('Bearer test-jwt')
          return
        }
      }
    }
  })

  it('无 token 时不注入 Authorization', async () => {
    const request = (await import('@/api/request')).default
    const config = { headers: {} }
    for (const h of request.interceptors.request.handlers) {
      if (h.fulfilled) {
        const result = await h.fulfilled(config)
        if (result?.headers) {
          expect(result.headers.Authorization).toBeUndefined()
        }
      }
    }
  })

  it('响应拦截器处理 401 清除 token', async () => {
    localStorage.setItem('token', 'old-token')
    localStorage.setItem('userInfo', 'test')
    const request = (await import('@/api/request')).default

    for (const h of request.interceptors.response.handlers) {
      if (h.rejected) {
        try { await h.rejected({ response: { status: 401 } }) } catch { break }

        expect(localStorage.getItem('token')).toBeNull()
        expect(localStorage.getItem('userInfo')).toBeNull()
        return
      }
    }
  })

  it('响应拦截器不拦截 非401 错误', async () => {
    localStorage.setItem('token', 'keep')
    localStorage.setItem('userInfo', 'keep')
    const request = (await import('@/api/request')).default

    for (const h of request.interceptors.response.handlers) {
      if (h.rejected) {
        try { await h.rejected({ response: { status: 500 } }) } catch {}
      }
    }

    expect(localStorage.getItem('token')).toBe('keep')
  })
})
