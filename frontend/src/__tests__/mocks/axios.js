import { vi } from 'vitest'
import axios from 'axios'
import { handlers } from '../mocks/handlers'

beforeEach(() => {
  vi.spyOn(axios, 'post').mockImplementation((url, data) => {
    const urlStr = typeof url === 'string' ? url : url.toString()
    const key = urlStr.replace('http://localhost:8080/api', '')
    const handler = handlers[key]
    if (handler?.POST) {
      const [status, response] = handler.POST(JSON.stringify(data))
      if (status >= 400) return Promise.reject({ response: { status }, ...response })
      return Promise.resolve({ data: response })
    }
    return Promise.reject(new Error(`No handler for POST ${key}`))
  })

  vi.spyOn(axios, 'get').mockImplementation((url) => {
    const urlStr = typeof url === 'string' ? url : url.toString()
    const key = urlStr.replace('http://localhost:8080/api', '')
    const handler = handlers[key]
    if (handler?.GET) {
      const token = localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
      const [status, response] = handler.GET(token)
      if (status >= 400) return Promise.reject({ response: { status }, ...response })
      return Promise.resolve({ data: response })
    }
    return Promise.reject(new Error(`No handler for GET ${key}`))
  })
})

afterEach(() => {
  vi.restoreAllMocks()
})
