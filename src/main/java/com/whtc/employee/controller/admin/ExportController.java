package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.EmployeeExportDTO;
import com.whtc.employee.entity.ExportTask;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报表导出控制器
 */
@RestController
@RequestMapping("/admin/export")
@Slf4j
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 获取导出字段选项
     */
    @GetMapping("/fields")
    public Result<List<EmployeeExportDTO.FieldOption>> getFieldOptions() {
        return Result.success(exportService.getFieldOptions());
    }

    /**
     * 创建导出任务
     */
    @PostMapping("/employee")
    public Result<Long> createExportTask(@RequestBody EmployeeExportDTO exportDTO) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        // 参数校验
        if (exportDTO.getFields() == null || exportDTO.getFields().isEmpty()) {
            exportDTO.setFields(EmployeeExportDTO.DEFAULT_FIELDS);
        }

        log.info("创建员工导出任务: userId={}, scope={}, fields={}",
                currentUser.getId(), exportDTO.getExportScope(), exportDTO.getFields());

        try {
            Long taskId = exportService.createExportTask(currentUser.getId(), exportDTO);

            // 异步执行导出
            exportService.asyncExportEmployee(taskId);

            return Result.success(taskId);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户的导出任务列表
     */
    @GetMapping("/tasks")
    public Result<List<ExportTask>> getExportTasks() {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        return Result.success(exportService.getUserExportTasks(currentUser.getId()));
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public Result<ExportTask> getTaskStatus(@PathVariable Long taskId) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        ExportTask task = exportService.getExportTask(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!task.getUserId().equals(currentUser.getId())) {
            return Result.error("无权访问此任务");
        }

        return Result.success(task);
    }

    /**
     * 下载导出文件
     */
    @GetMapping("/download/{taskId}")
    public void downloadFile(@PathVariable Long taskId, HttpServletResponse response) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            writeErrorResponse(response, 401, "用户未登录");
            return;
        }

        try {
            String filePath = exportService.getTaskFilePath(taskId, currentUser.getId());
            File file = new File(filePath);

            if (!file.exists()) {
                writeErrorResponse(response, 404, "文件不存在");
                return;
            }

            // 获取导出任务信息用于文件名
            ExportTask task = exportService.getExportTask(taskId);
            String fileName = task != null ? task.getFileName() : "员工报表.xlsx";

            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setContentLength((int) file.length());

            // 写入文件内容
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                }
            }
            response.getOutputStream().flush();

        } catch (IllegalArgumentException e) {
            writeErrorResponse(response, 400, e.getMessage());
        } catch (IOException e) {
            log.error("下载文件失败: taskId={}", taskId, e);
            writeErrorResponse(response, 500, "文件下载失败");
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            String json = String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", status, message);
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error("写入错误响应失败", e);
        }
    }
}
