import request from '@/utils/request'

export const getSupplierPage = (params) => {
  return request({
    url: '/admin/supplier/page',
    method: 'get',
    params
  })
}

export const getSupplierById = (id) => {
  return request({
    url: `/admin/supplier/${id}`,
    method: 'get'
  })
}

export const addSupplier = (data) => {
  return request({
    url: '/admin/supplier',
    method: 'post',
    data
  })
}

export const updateSupplier = (data) => {
  return request({
    url: '/admin/supplier',
    method: 'put',
    data
  })
}

export const deleteSupplier = (id) => {
  return request({
    url: `/admin/supplier/${id}`,
    method: 'delete'
  })
}
