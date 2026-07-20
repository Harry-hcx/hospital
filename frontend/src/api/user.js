import request from './request'

// 个人资料
export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

// 就诊人
export function getFamilyMembers() {
  return request.get('/family-members')
}

export function createFamilyMember(data) {
  return request.post('/family-members', data)
}

export function updateFamilyMember(id, data) {
  return request.put(`/family-members/${id}`, data)
}

export function deleteFamilyMember(id) {
  return request.delete(`/family-members/${id}`, { data: {} })
}

// 评价
export function getMyReviews(params) {
  return request.get('/reviews/my', { params })
}

export function createReview(data) {
  return request.post('/reviews', data)
}

// 关注
export function getMyFollows(params) {
  return request.get('/follow/my', { params })
}

export function createFollow(type, id) {
  const resource = { 1: 'hospital', 2: 'doctor', 3: 'disease' }[type]
  return request.post(`/follow/${resource}/${id}`, {})
}

export function deleteFollow(type, id) {
  return request.delete(`/follow/${type}/${id}`, { data: {} })
}

// 消息
export function getMessages(params) {
  return request.get('/messages', { params })
}

export function markMessageRead(id) {
  return request.post(`/messages/${id}/read`, {})
}

// 反馈
export function getMyFeedbacks(params) {
  return request.get('/feedbacks/my', { params })
}

export function createFeedback(data) {
  return request.post('/feedbacks', data)
}
