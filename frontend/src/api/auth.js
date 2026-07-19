import request from './request'

export const sendCaptchaApi = (data) => request.post('/auth/sendCaptcha', data)
export const registerApi = (data) => request.post('/auth/register', data)
export const loginApi = (data) => request.post('/auth/login', data)
export const getMeApi = () => request.get('/auth/me')
export const changePasswordApi = (data) => request.post('/auth/change-password', data)
export const logoutApi = () => request.post('/auth/logout', {})
