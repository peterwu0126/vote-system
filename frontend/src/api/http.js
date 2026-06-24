import axios from 'axios'

// 統一 Axios 實例設定
const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 回應攔截器：統一解開 ApiResponse 包裝，並將錯誤訊息正規化
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      '網路連線異常，請稍後再試'
    return Promise.reject(new Error(message))
  }
)

export default http
