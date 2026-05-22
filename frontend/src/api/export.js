import request from '@/utils/request'

/**
 * 获取导出字段选项
 */
export const getExportFields = () => {
  return request({
    url: '/admin/export/fields',
    method: 'get'
  })
}

/**
 * 创建导出任务
 * @param {Object} data 导出参数
 */
export const createExportTask = (data) => {
  return request({
    url: '/admin/export/employee',
    method: 'post',
    data
  })
}

/**
 * 获取导出任务列表
 */
export const getExportTasks = () => {
  return request({
    url: '/admin/export/tasks',
    method: 'get'
  })
}

/**
 * 查询任务状态
 * @param {number} taskId 任务ID
 */
export const getTaskStatus = (taskId) => {
  return request({
    url: `/admin/export/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 下载导出文件
 * @param {number} taskId 任务ID
 */
export const downloadExportFile = (taskId) => {
  return request({
    url: `/admin/export/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  })
}
