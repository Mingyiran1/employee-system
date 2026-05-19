/**
 * 正则表达式常量（与后端 RegexConstant 保持一致）
 */

/**
 * 手机号正则：1开头，第二位3-9，共11位
 */
export const PHONE = /^1[3-9]\d{9}$/
export const PHONE_MESSAGE = '手机号格式不正确'

/**
 * 邮箱正则
 */
export const EMAIL = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
export const EMAIL_MESSAGE = '邮箱格式不正确'

/**
 * 身份证号正则（宽松）：18位，前17位数字，最后一位数字或X/x
 */
export const ID_CARD = /^\d{17}[\dXx]$/
export const ID_CARD_MESSAGE = '身份证号格式不正确（18位，最后一位可为X）'

/**
 * 固定电话/手机号通用正则
 */
export const PHONE_OR_TEL = /^(1[3-9]\d{9})|(0\d{2,3}-?\d{7,8})$/
export const PHONE_OR_TEL_MESSAGE = '电话格式不正确'

/**
 * 创建 Element Plus 表单校验规则
 * @param {RegExp} pattern - 正则表达式
 * @param {String} message - 错误提示消息
 * @param {Boolean} required - 是否必填
 * @returns {Array} 校验规则数组
 */
export const createRules = (pattern, message, required = false) => {
  const rules = []
  if (required) {
    rules.push({ required: true, message: '此项为必填项', trigger: 'blur' })
  }
  rules.push({ pattern, message, trigger: 'blur' })
  return rules
}
