import request from '@/utils/request'

export const getDepartmentList = () => {
  return request({
    url: '/admin/department/list',
    method: 'get'
  })
}

export const addDepartment = (data) => {
  return request({
    url: '/admin/department',
    method: 'post',
    data
  })
}

export const updateDepartment = (data) => {
  return request({
    url: '/admin/department',
    method: 'put',
    data
  })
}

export const deleteDepartment = (id) => {
  return request({
    url: `/admin/department/${id}`,
    method: 'delete'
  })
}
