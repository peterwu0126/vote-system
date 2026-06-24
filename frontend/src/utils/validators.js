/**
 * 前端輸入驗證與 XSS 防護工具
 *
 * 1. escapeHtml：渲染使用者輸入內容前進行 HTML escape (雙重防護，
 *    即使 Vue 模板預設已會 escape 插值內容，這裡額外提供工具給
 *    需要手動組字串或寫入 innerHTML 以外管道使用)。
 * 2. 驗證規則對應後端 Bean Validation，提前在前端擋掉明顯不合法輸入，
 *    減少不必要的請求往返，但後端仍為最終防線。
 */

export function escapeHtml(str) {
  if (str == null) return ''
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 投票項目名稱：1~100 字元，禁止 < > 等 HTML 特殊符號
export function validateItemName(name) {
  const trimmed = (name || '').trim()
  if (!trimmed) return '投票項目名稱不可為空'
  if (trimmed.length > 100) return '投票項目名稱長度不可超過 100 字元'
  if (/[<>]/.test(trimmed)) return '投票項目名稱不可包含 < 或 > 字元'
  return ''
}

// 投票人姓名：1~50 字元，僅允許中英文、數字、空白、底線、連字號
export function validateVoterName(name) {
  const trimmed = (name || '').trim()
  if (!trimmed) return '請輸入您的姓名'
  if (trimmed.length > 50) return '姓名長度不可超過 50 字元'
  if (!/^[\p{L}0-9 _-]{1,50}$/u.test(trimmed)) return '姓名包含不允許的字元'
  return ''
}
