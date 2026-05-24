import request from '@/utils/request'

export const getPremiumConfigList = () => {
  return request({
    url: '/admin/premium-config/list',
    method: 'get'
  })
}

export const getPremiumConfigById = (id) => {
  return request({
    url: `/admin/premium-config/${id}`,
    method: 'get'
  })
}

export const getPremiumConfigByJobType = (jobType) => {
  return request({
    url: `/admin/premium-config/job-type/${jobType}`,
    method: 'get'
  })
}

export const addPremiumConfig = (data) => {
  return request({
    url: '/admin/premium-config',
    method: 'post',
    data
  })
}

export const updatePremiumConfig = (data) => {
  return request({
    url: '/admin/premium-config',
    method: 'put',
    data
  })
}

export const deletePremiumConfig = (id) => {
  return request({
    url: `/admin/premium-config/${id}`,
    method: 'delete'
  })
}
