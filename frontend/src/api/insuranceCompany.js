import request from '@/utils/request'

// 获取保险公司分页列表
export const getInsuranceCompanyPage = (params) => {
  return request({
    url: '/admin/insurance-company/page',
    method: 'get',
    params
  })
}

// 获取所有保险公司列表
export const getInsuranceCompanyList = () => {
  return request({
    url: '/admin/insurance-company/list',
    method: 'get'
  })
}

// 根据ID查询保险公司
export const getInsuranceCompanyById = (id) => {
  return request({
    url: `/admin/insurance-company/${id}`,
    method: 'get'
  })
}

// 新增保险公司
export const addInsuranceCompany = (data) => {
  return request({
    url: '/admin/insurance-company',
    method: 'post',
    data
  })
}

// 更新保险公司
export const updateInsuranceCompany = (data) => {
  return request({
    url: '/admin/insurance-company',
    method: 'put',
    data
  })
}

// 删除保险公司
export const deleteInsuranceCompany = (id) => {
  return request({
    url: `/admin/insurance-company/${id}`,
    method: 'delete'
  })
}

// 别名导出（兼容 InsuredCompany.vue 导入方式）
export const getCompanyList = getInsuranceCompanyList
export const getCompanyPage = getInsuranceCompanyPage
export const getCompanyById = getInsuranceCompanyById
export const addCompany = addInsuranceCompany
export const updateCompany = updateInsuranceCompany
export const deleteCompany = deleteInsuranceCompany
