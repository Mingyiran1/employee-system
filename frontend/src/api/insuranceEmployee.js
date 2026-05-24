import request from '@/utils/request'

const downloadBlobFile = (blob, fileName) => {
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

// 保险员工分页查询
export const getInsuranceEmployeePage = (params) => {
  return request({
    url: '/admin/insurance-employee/page',
    method: 'get',
    params
  })
}

// 根据ID查询保险员工
export const getInsuranceEmployeeById = (id) => {
  return request({
    url: `/admin/insurance-employee/${id}`,
    method: 'get'
  })
}

// 新增保险员工
export const addInsuranceEmployee = (data) => {
  return request({
    url: '/admin/insurance-employee',
    method: 'post',
    data
  })
}

// 更新保险员工
export const updateInsuranceEmployee = (data) => {
  return request({
    url: '/admin/insurance-employee',
    method: 'put',
    data
  })
}

// 删除保险员工
export const deleteInsuranceEmployee = (id) => {
  return request({
    url: `/admin/insurance-employee/${id}`,
    method: 'delete'
  })
}

// 批量删除保险员工
export const deleteInsuranceEmployeeBatch = (ids) => {
  return request({
    url: '/admin/insurance-employee/batch',
    method: 'delete',
    params: { ids }
  })
}

// 获取所有保险员工列表
export const getInsuranceEmployeeListAll = () => {
  return request({
    url: '/admin/insurance-employee/list-all',
    method: 'get'
  })
}

// 获取所有保险员工列表
export const getInsuranceEmployeeList = (params) => {
  return request({
    url: '/admin/insurance-employee/list-all',
    method: 'get',
    params
  })
}

// 导出保险员工Excel
export const exportInsuranceEmployee = async (params) => {
  const blob = await request({
    url: '/admin/insurance-employee/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
  downloadBlobFile(blob, `保险员工导出_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

// Excel导入保险员工
export const importInsuranceEmployee = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/insurance-employee/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载导入模板
export const downloadTemplate = async () => {
  const blob = await request({
    url: '/admin/insurance-employee/template',
    method: 'get',
    responseType: 'blob'
  })
  downloadBlobFile(blob, '保险员工导入模板.xlsx')
}

// 计算日保费
export const calculateDailyPremium = (annualPremium, calcType = 1) => {
  return request({
    url: '/admin/insurance-employee/calculate-daily-premium',
    method: 'get',
    params: { annualPremium, calcType }
  })
}

// 别名导出（兼容 Employee.vue 的导入方式）
export const getEmployeePage = getInsuranceEmployeePage
export const getEmployeeById = getInsuranceEmployeeById
export const addEmployee = addInsuranceEmployee
export const updateEmployee = updateInsuranceEmployee
export const deleteEmployee = deleteInsuranceEmployee
export const batchDeleteEmployee = deleteInsuranceEmployeeBatch
export const getEmployeeList = getInsuranceEmployeeList
