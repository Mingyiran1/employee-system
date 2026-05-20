import request from '@/utils/request'

// 获取未读消息数量
export const getUnreadCount = () => {
  return request({
    url: '/admin/message/unread-count',
    method: 'get'
  })
}

// 获取消息列表
export const getMessageList = (params) => {
  return request({
    url: '/admin/message/list',
    method: 'get',
    params
  })
}

// 标记消息为已读
export const markAsRead = (messageId) => {
  return request({
    url: `/admin/message/read/${messageId}`,
    method: 'post'
  })
}

// 标记所有消息为已读
export const markAllAsRead = () => {
  return request({
    url: '/admin/message/read-all',
    method: 'post'
  })
}
