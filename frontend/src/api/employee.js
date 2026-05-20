import request from '@/utils/request'

export const getEmployeePage = (params) => {
  return request({
    url: '/admin/employee/page',
    method: 'get',
    params
  })
}

export const getEmployeeById = (id) => {
  return request({
    url: `/admin/employee/${id}`,
    method: 'get'
  })
}

export const addEmployee = (data) => {
  return request({
    url: '/admin/employee',
    method: 'post',
    data
  })
}

export const updateEmployee = (data) => {
  return request({
    url: '/admin/employee',
    method: 'put',
    data
  })
}

export const deleteEmployee = (id) => {
  return request({
    url: `/admin/employee/${id}`,
    method: 'delete'
  })
}

export const deleteEmployeeBatch = (ids) => {
  return request({
    url: '/admin/employee/batch',
    method: 'delete',
    params: { ids }
  })
}

// 获取所有员工列表（用于审批场景，不带数据权限限制）
export const getEmployeeListAll = () => {
  return request({
    url: '/admin/employee/list-all',
    method: 'get'
  })
}

// 获取当前登录用户对应的员工信息
export const getCurrentEmployee = () => {
  return request({
    url: '/admin/employee/current',
    method: 'get'
  })
}
