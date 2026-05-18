import axios from 'axios'

const api = axios.create({
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      if (window.location.pathname !== '/' && window.location.pathname !== '/login') {
        alert('로그인이 만료되었습니다. 다시 로그인해주세요.')
        window.location.href = '/'
      }
    }
    return Promise.reject(error)
  },
)

export default api
