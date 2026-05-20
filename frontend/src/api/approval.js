import request from '@/utils/request'

// 发起审批
export const startApproval = (data) => {
  return request({
    url: '/admin/approval/start',
    method: 'post',
    data
  })
}

// 审批处理（通过/拒绝）
export const processApproval = (recordId, data) => {
  return request({
    url: `/admin/approval/process/${recordId}`,
    method: 'post',
    data
  })
}

// 获取待审批列表
export const getPendingApprovals = (params) => {
  return request({
    url: '/admin/approval/pending',
    method: 'get',
    params
  })
}

// 获取我发起的审批列表
export const getMyApprovals = (params) => {
  return request({
    url: '/admin/approval/my',
    method: 'get',
    params
  })
}

// 获取审批详情
export const getApprovalDetail = (recordId) => {
  return request({
    url: `/admin/approval/detail/${recordId}`,
    method: 'get'
  })
}

// 获取业务审批状态
export const getApprovalStatus = (businessType, businessId) => {
  return request({
    url: '/admin/approval/status',
    method: 'get',
    params: { businessType, businessId }
  })
}

// 撤销审批
export const cancelApproval = (recordId) => {
  return request({
    url: `/admin/approval/cancel/${recordId}`,
    method: 'post'
  })
}
