import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  delete: vi.fn(),
}))

vi.mock('@/api/request', () => ({ default: request }))

import { createAppointment, getAppointmentSuccess, payAppointment } from '@/api/appointment'
import { createConsult, getConsultSuccess, payConsult } from '@/api/consult'
import { createFeedback, createFollow, deleteFollow, getMyFeedbacks } from '@/api/user'

describe('workflow API contracts', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses the documented appointment endpoints and payload unchanged', () => {
    const payload = {
      doctorId: 1,
      hospitalId: 1,
      patientId: 2,
      appointmentDate: '2026-07-20',
      appointmentTime: '上午',
      diseaseDesc: '头痛',
    }
    createAppointment(payload)
    getAppointmentSuccess('A001')
    payAppointment('A001', { payMethod: 'wechat' })

    expect(request.post).toHaveBeenNthCalledWith(1, '/appointments', payload)
    expect(request.get).toHaveBeenCalledWith('/appointments/A001/success')
    expect(request.post).toHaveBeenNthCalledWith(2, '/appointments/A001/pay', { payMethod: 'wechat' })
  })

  it('uses the documented consultation endpoints and payload unchanged', () => {
    const payload = {
      doctorId: 1,
      patientName: '张三',
      patientPhone: '13800138000',
      diseaseDesc: '复诊',
      appointmentTime: '2026-07-20 14:00:00',
      duration: 30,
    }
    createConsult(payload)
    getConsultSuccess('C001')
    payConsult('C001', { payMethod: 'alipay' })

    expect(request.post).toHaveBeenNthCalledWith(1, '/consults', payload)
    expect(request.get).toHaveBeenCalledWith('/consults/C001/success')
    expect(request.post).toHaveBeenNthCalledWith(2, '/consults/C001/pay', { payMethod: 'alipay' })
  })

  it('uses typed follow creation and the documented feedback list endpoint', () => {
    createFollow(1, 8)
    createFollow(2, 9)
    createFollow(3, 10)
    deleteFollow(2, 9)
    getMyFeedbacks({ page: 1, pageSize: 10 })
    createFeedback({ feedbackType: 2, content: '建议' })

    expect(request.post).toHaveBeenNthCalledWith(1, '/follow/hospital/8', {})
    expect(request.post).toHaveBeenNthCalledWith(2, '/follow/doctor/9', {})
    expect(request.post).toHaveBeenNthCalledWith(3, '/follow/disease/10', {})
    expect(request.delete).toHaveBeenCalledWith('/follow/2/9', { data: {} })
    expect(request.get).toHaveBeenCalledWith('/feedbacks/my', { params: { page: 1, pageSize: 10 } })
    expect(request.post).toHaveBeenNthCalledWith(4, '/feedbacks', { feedbackType: 2, content: '建议' })
  })
})
