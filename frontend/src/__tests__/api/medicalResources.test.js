import { beforeEach, describe, expect, it, vi } from 'vitest'

const get = vi.hoisted(() => vi.fn())

vi.mock('@/api/request', () => ({
  default: { get },
}))

import { getHospitals, getHospitalDoctors } from '@/api/hospital'
import { getDoctors } from '@/api/doctor'
import { getDiseases } from '@/api/disease'
import { getArticles } from '@/api/article'
import { globalSearch } from '@/api/search'

describe('medical resource API contracts', () => {
  beforeEach(() => get.mockClear())

  it('passes combined hospital filters and pagination', () => {
    const params = { page: 2, pageSize: 10, departmentId: 3, keyword: '中心', level: '三级甲等', province: '浙江', city: '杭州' }
    getHospitals(params)
    expect(get).toHaveBeenCalledWith('/hospitals', { params })
  })

  it('passes doctor, disease and article filters without dropping pagination', () => {
    const params = { page: 3, pageSize: 12, departmentId: 4, keyword: '张' }
    getDoctors(params)
    getDiseases(params)
    getArticles(params)
    expect(get).toHaveBeenNthCalledWith(1, '/doctors', { params })
    expect(get).toHaveBeenNthCalledWith(2, '/diseases', { params })
    expect(get).toHaveBeenNthCalledWith(3, '/articles', { params })
  })

  it('uses the hospital relation endpoint with supported pagination only', () => {
    const params = { page: 2, pageSize: 10 }
    getHospitalDoctors(8, params)
    expect(get).toHaveBeenNthCalledWith(1, '/hospitals/8/doctors', { params })
  })

  it('passes search type and pagination to the grouped search endpoint', () => {
    const params = { keyword: '心脏', type: 'doctor', page: 2, pageSize: 10 }
    globalSearch(params)
    expect(get).toHaveBeenNthCalledWith(1, '/search/global', { params })
  })
})
