import { vi } from 'vitest'

const mockUser = {
  id: 1,
  phone: '13800138000',
  realName: '张三',
  avatar: 'img/default-avatar.png',
}

const mockToken = 'eyJhbGciOiJIUzI1NiJ9.mock-token'

export const handlers = {
  '/auth/login': {
    POST: (data) => {
      const { phone, password } = JSON.parse(data)
      if (phone === '13800138000' && password === '123456') {
        return [200, { code: 200, message: '登录成功', data: { token: mockToken, userInfo: mockUser } }]
      }
      return [400, { code: 400, message: '手机号或密码错误' }]
    },
  },

  '/auth/register': {
    POST: (data) => {
      const { phone } = JSON.parse(data)
      if (phone === '13800138000') {
        return [400, { code: 400, message: '手机号已注册' }]
      }
      return [200, { code: 200, message: '注册成功', data: { userId: 2 } }]
    },
  },

  '/auth/me': {
    GET: (token) => {
      if (token === `Bearer ${mockToken}`) {
        return [200, { code: 200, data: { id: 1, phone: '13800138000', realName: '张三' } }]
      }
      return [401, { code: 401, message: '未登录' }]
    },
  },

  '/auth/change-password': {
    POST: (data) => {
      const { oldPassword } = JSON.parse(data)
      if (oldPassword === 'wrong') {
        return [400, { code: 400, message: '原密码错误' }]
      }
      return [200, { code: 200, message: '密码修改成功' }]
    },
  },

  '/auth/logout': {
    POST: () => {
      return [200, { code: 200, message: '退出成功' }]
    },
  },
}

export { mockUser, mockToken }
