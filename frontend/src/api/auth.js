import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}
