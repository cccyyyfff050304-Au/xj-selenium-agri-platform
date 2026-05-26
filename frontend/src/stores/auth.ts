import { defineStore } from 'pinia'
import http from '@/api/http'

interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('xj-agri-token') || '',
    user: null as UserInfo | null
  }),
  actions: {
    async login(username: string, password: string) {
      const data = await http.post<any, { token: string; username: string; realName: string; role: string }>('/auth/login', {
        username,
        password
      })
      this.token = data.token
      this.user = {
        id: 0,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      localStorage.setItem('xj-agri-token', data.token)
    },
    async loadUser() {
      if (!this.token) return
      this.user = await http.get<any, UserInfo>('/auth/me')
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('xj-agri-token')
    }
  }
})
