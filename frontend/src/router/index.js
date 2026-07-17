import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/RegisterView.vue') },
  { path: '/change-password', name: 'ChangePassword', component: () => import('@/views/ChangePasswordView.vue'), meta: { auth: true } },
  { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
  { path: '/hospitals', name: 'HospitalList', component: () => import('@/views/HospitalListView.vue') },
  { path: '/hospital/:id', name: 'HospitalDetail', component: () => import('@/views/HospitalDetailView.vue') },
  { path: '/doctors', name: 'DoctorList', component: () => import('@/views/DoctorListView.vue') },
  { path: '/doctor/:id', name: 'DoctorDetail', component: () => import('@/views/DoctorDetailView.vue') },
  { path: '/diseases', name: 'DiseaseList', component: () => import('@/views/DiseaseListView.vue') },
  { path: '/disease/:id', name: 'DiseaseDetail', component: () => import('@/views/DiseaseDetailView.vue') },
  { path: '/articles', name: 'ArticleList', component: () => import('@/views/ArticleListView.vue') },
  { path: '/article/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetailView.vue') },
  { path: '/search', name: 'Search', component: () => import('@/views/SearchView.vue') },
  { path: '/reservation', name: 'Reservation', component: () => import('@/views/ReservationView.vue'), meta: { auth: true } },
  { path: '/reservation/pay/:orderNo', name: 'ReservationPay', component: () => import('@/views/ReservationPayView.vue'), meta: { auth: true } },
  { path: '/reservation/success/:orderNo', name: 'ReservationSuccess', component: () => import('@/views/ReservationSuccessView.vue'), meta: { auth: true } },
  { path: '/consult', name: 'Consult', component: () => import('@/views/ConsultView.vue'), meta: { auth: true } },
  { path: '/consult/pay/:orderNo', name: 'ConsultPay', component: () => import('@/views/ConsultPayView.vue'), meta: { auth: true } },
  { path: '/consult/success/:orderNo', name: 'ConsultSuccess', component: () => import('@/views/ConsultSuccessView.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/ProfileView.vue'), meta: { auth: true } },
  { path: '/family-members', name: 'FamilyMembers', component: () => import('@/views/FamilyMemberView.vue'), meta: { auth: true } },
  { path: '/my-appointments', name: 'MyAppointments', component: () => import('@/views/MyAppointmentView.vue'), meta: { auth: true } },
  { path: '/my-consults', name: 'MyConsults', component: () => import('@/views/MyConsultView.vue'), meta: { auth: true } },
  { path: '/my-reviews', name: 'MyReviews', component: () => import('@/views/MyReviewView.vue'), meta: { auth: true } },
  { path: '/follow', name: 'Follow', component: () => import('@/views/FollowView.vue'), meta: { auth: true } },
  { path: '/messages', name: 'Messages', component: () => import('@/views/MessageView.vue'), meta: { auth: true } },
  { path: '/feedback', name: 'Feedback', component: () => import('@/views/FeedbackView.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('token')

  if (to.meta.auth && !isLoggedIn) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  } else {
    next()
  }
})

export default router
