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
