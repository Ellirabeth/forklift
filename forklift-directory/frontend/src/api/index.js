import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export default {
  // Погрузчики
  getForklifts(params) {
    return apiClient.get('/forklifts', { params })
  },

  searchForklifts(number) {
    return apiClient.get('/forklifts/search', { params: { number } })
  },

  createForklift(data) {
    return apiClient.post('/forklifts', data)
  },

  updateForklift(id, data) {
    return apiClient.put(`/forklifts/${id}`, data)
  },

  deleteForklift(id) {
    return apiClient.delete(`/forklifts/${id}`)
  },

  // Простои
  getDowntimes(forkliftId) {
    return apiClient.get(`/forklifts/${forkliftId}/downtimes`)
  },

  createDowntime(data) {
    return apiClient.post('/downtimes', data)
  },

  updateDowntime(id, data) {
    return apiClient.put(`/downtimes/${id}`, data)
  },

  deleteDowntime(id) {
    return apiClient.delete(`/downtimes/${id}`)
  }
}