import axios from 'axios'

// 配置axios基础URL，根据环境变量或默认值
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('game_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => {
    console.log('API响应:', response.data)
    return response.data
  },
  error => {
    console.error('API错误:', error)
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      console.error('错误状态码:', status)
      console.error('错误数据:', data)
      if (status === 401) {
        // token过期，清除本地数据并跳转登录
        localStorage.removeItem('game_token')
        localStorage.removeItem('game_username')
        window.location.href = '/'
      }
      // 返回错误对象，包含message字段
      const errorObj = data || { message: error.message }
      return Promise.reject(errorObj)
    }
    return Promise.reject({ message: error.message || '网络错误' })
  }
)

// ============ 用户相关API ============
export const userAPI = {
  // 注册
  register(username, password) {
    return api.post('/user/register', { username, password })
  },
  // 登录
  login(username, password) {
    return api.post('/user/login', { username, password })
  },
  // 重置密码
  resetPassword(username, newPassword) {
    return api.post('/user/reset-password', { username, newPassword })
  },
  // 获取当前用户信息
  getCurrentUser() {
    return api.get('/user/current')
  }
}

// ============ 游戏数据API ============
export const gameAPI = {
  // 获取用户游戏数据
  getUserGameData(userId, gameType) {
    return api.get(`/game/${gameType}/${userId}`)
  },
  // 保存用户游戏数据
  saveUserGameData(userId, gameType, data) {
    return api.post(`/game/${gameType}/${userId}`, data)
  },
  // 更新用户游戏数据
  updateUserGameData(userId, gameType, data) {
    return api.put(`/game/${gameType}/${userId}`, data)
  }
}

// ============ 排行榜API ============
export const rankAPI = {
  // 获取排行榜
  getRankList(gameType, mode, limit = 10) {
    return api.get(`/rank/${gameType}`, { params: { mode, limit } })
  },
  // 提交排行榜记录
  submitRank(gameType, data) {
    return api.post(`/rank/${gameType}`, data)
  },
  // 获取特定关卡的排行榜
  getRankListByLevel(gameType, level, mode, limit = 10) {
    return api.get(`/rank/${gameType}/level/${level}`, { params: { mode, limit } })
  }
}

// ============ 商城API ============
export const shopAPI = {
  // 获取商品列表
  getItems(category) {
    return api.get('/shop/items', { params: { category } })
  },
  // 获取用户拥有的皮肤
  getOwnedSkins(userId) {
    return api.get(`/shop/user/${userId}/skins`)
  },
  // 购买商品
  purchase(userId, itemId) {
    return api.post(`/shop/purchase`, { userId, itemId })
  },
  // 获取用户金币
  getCoins(userId) {
    return api.get(`/shop/user/${userId}/coins`)
  },
  // 更新用户金币
  updateCoins(userId, coins) {
    return api.put(`/shop/user/${userId}/coins`, { coins })
  }
}

// ============ 游戏特定API ============
export const whackAMoleAPI = {
  // 获取打地鼠游戏数据
  getGameData(userId) {
    return api.get(`/game/whackamole/${userId}`)
  },
  // 保存打地鼠游戏数据
  saveGameData(userId, data) {
    return api.post(`/game/whackamole/${userId}`, data)
  }
}

export const gobangAPI = {
  // 获取五子棋游戏数据
  getGameData(userId) {
    return api.get(`/game/gobang/${userId}`)
  },
  // 保存五子棋游戏数据
  saveGameData(userId, data) {
    return api.post(`/game/gobang/${userId}`, data)
  }
}

export const sudokuAPI = {
  // 获取数独游戏数据
  getGameData(userId) {
    return api.get(`/game/sudoku/${userId}`)
  },
  // 保存数独游戏数据
  saveGameData(userId, data) {
    return api.post(`/game/sudoku/${userId}`, data)
  }
}

export default api

