package com.whtc.employee.service;

import com.whtc.employee.dto.EmployeeExportDTO;
import com.whtc.employee.entity.ExportTask;

import java.util.List;

/**
 * 报表导出服务
 */
public interface ExportService {

    /**
     * 创建导出任务
     *
     * @param userId 用户ID
     * @param exportDTO 导出参数
     * @return 任务ID
     */
    Long createExportTask(Long userId, EmployeeExportDTO exportDTO);

    /**
     * 异步执行导出任务
     *
     * @param taskId 任务ID
     */
    void asyncExportEmployee(Long taskId);

    /**
     * 获取用户的导出任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ExportTask> getUserExportTasks(Long userId);

    /**
     * 根据ID获取导出任务
     *
     * @param taskId 任务ID
     * @return 导出任务
     */
    ExportTask getExportTask(Long taskId);

    /**
     * 获取任务文件路径
     *
     * @param taskId 任务ID
     * @param userId 用户ID（权限校验）
     * @return 文件路径
     */
    String getTaskFilePath(Long taskId, Long userId);

    /**
     * 获取所有可选字段选项
     *
     * @return 字段选项列表
     */
    List<EmployeeExportDTO.FieldOption> getFieldOptions();
}
