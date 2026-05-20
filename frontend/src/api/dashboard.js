import request from '@/utils/request'

// 获取员工概览数据
export const getDashboardOverview = () => {
  return request({
    url: '/admin/dashboard/overview',
    method: 'get'
  })
}

// 获取部门分布数据
export const getDeptDistribution = () => {
  return request({
    url: '/admin/dashboard/dept-distribution',
    method: 'get'
  })
}

// 获取性别分布数据
export const getGenderDistribution = () => {
  return request({
    url: '/admin/dashboard/gender-distribution',
    method: 'get'
  })
}

// 获取入职趋势数据
export const getEntryTrend = () => {
  return request({
    url: '/admin/dashboard/entry-trend',
    method: 'get'
  })
}

// 获取待审批数量
export const getApprovalPending = () => {
  return request({
    url: '/admin/dashboard/approval-pending',
    method: 'get'
  })
}

// 获取所有仪表盘数据（聚合接口）
export const getDashboardAll = () => {
  return request({
    url: '/admin/dashboard/all',
    method: 'get'
  })
}
