import http from './http'

/**
 * 投票項目管理 API (後台 CRUD)
 */
export const itemApi = {
  list() {
    return http.get('/items')
  },
  get(itemId) {
    return http.get(`/items/${itemId}`)
  },
  create(itemName) {
    return http.post('/items', { itemName })
  },
  update(itemId, itemName) {
    return http.patch(`/items/${itemId}`, { itemName })
  },
  remove(itemId) {
    return http.delete(`/items/${itemId}`)
  }
}

/**
 * 投票 API (使用者投票 / 紀錄查詢)
 */
export const voteApi = {
  cast(voterName, itemIds) {
    return http.post('/votes', { voterName, itemIds })
  },
  records(itemId) {
    return http.get('/votes/records', { params: itemId ? { itemId } : {} })
  }
}
