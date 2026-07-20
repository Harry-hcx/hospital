import request from './request'

export function createConsult(data) {
  return request.post('/consults', data)
}

export function getConsultDetail(orderNo) {
  return request.get(`/consults/${orderNo}`)
}

export function getConsultSuccess(orderNo) {
  return request.get(`/consults/${orderNo}/success`)
}

export function cancelConsult(orderNo) {
  return request.post(`/consults/${orderNo}/cancel`, {})
}

export function payConsult(orderNo, data) {
  return request.post(`/consults/${orderNo}/pay`, data)
}

export function getMyConsults(params) {
  return request.get('/consults/my', { params })
}
