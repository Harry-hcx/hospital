import request from './request'

export function createAppointment(data) {
  return request.post('/appointments', data)
}

export function getAppointmentDetail(orderNo) {
  return request.get(`/appointments/${orderNo}`)
}

export function getAppointmentSuccess(orderNo) {
  return request.get(`/appointments/${orderNo}/success`)
}

export function cancelAppointment(orderNo) {
  return request.post(`/appointments/${orderNo}/cancel`, {})
}

export function completeAppointment(orderNo) {
  return request.post(`/appointments/${orderNo}/complete`, {})
}

export function payAppointment(orderNo, data) {
  return request.post(`/appointments/${orderNo}/pay`, data)
}

export function getMyAppointments(params) {
  return request.get('/appointments/my', { params })
}
