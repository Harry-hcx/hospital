import { defineStore } from 'pinia'
import { loginApi, getMeApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => {
    let userInfo = null
    try {
      const raw = localStorage.getItem('userInfo')
      userInfo = raw ? JSON.parse(raw) : null
    } catch { userInfo = null }
    return {
      token: localStorage.getItem('token') || '',
      userInfo,
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    maskedPhone: (state) => {
      const phone = state.userInfo?.phone || ''
      return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3')
    },
  },

  actions: {
    async login(phone, password) {
      const res = await loginApi({ phone, password })
      this.token = res.data.token
      this.userInfo = res.data.userInfo
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    async fetchUser() {
      try {
        const res = await getMeApi()
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      } catch {
        this.clearAuth()
      }
    },

    logout() {
      this.clearAuth()
    },

    clearAuth() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
