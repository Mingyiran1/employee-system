package com.whtc.employee.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.context.DataScopeContext;
import com.whtc.employee.dto.EmployeeExportDTO;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.Department;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.entity.ExportTask;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.DepartmentMapper;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.mapper.ExportTaskMapper;
import com.whtc.employee.service.ExportService;
import com.whtc.employee.service.MessageService;
import com.whtc.employee.vo.EmployeeExportVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表导出服务实现
 */
@Service
@Slf4j
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.export.path:./temp/exports}")
    private String exportPath;

    // 最大导出数量限制
    private static final int MAX_EXPORT_COUNT = 10000;
    // 每批次读取数量
    private static final int BATCH_SIZE = 500;
    // 单个用户最大进行中任务数
    private static final int MAX_RUNNING_TASKS_PER_USER = 2;

    @Override
    public Long createExportTask(Long userId, EmployeeExportDTO exportDTO) {
        // 检查用户并发任务数
        int runningTasks = exportTaskMapper.countUserRunningTasks(userId);
        if (runningTasks >= MAX_RUNNING_TASKS_PER_USER) {
            throw new IllegalStateException("您已有" + runningTasks + "个进行中的导出任务，请等待完成后再创建新任务");
        }

        // 校验字段白名单
        List<String> validFields = EmployeeExportDTO.ALL_FIELD_OPTIONS.stream()
                .map(EmployeeExportDTO.FieldOption::getValue)
                .toList();
        List<String> requestedFields = exportDTO.getFields();
        if (requestedFields != null && !requestedFields.isEmpty()) {
            for (String field : requestedFields) {
                if (!validFields.contains(field)) {
                    throw new IllegalArgumentException("非法的导出字段: " + field);
                }
            }
        }

        ExportTask task = new ExportTask();
        task.setUserId(userId);
        task.setStatus(ExportTask.STATUS_PENDING);
        try {
            task.setFilterParams(objectMapper.writeValueAsString(exportDTO));
            task.setExportFields(objectMapper.writeValueAsString(exportDTO.getFields()));
        } catch (Exception e) {
            throw new RuntimeException("序列化导出参数失败", e);
        }

        // 生成文件名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = String.format("员工报表_%s.xlsx", timestamp);
        task.setFileName(fileName);

        // 估算总记录数
        int totalCount = estimateTotalCount(exportDTO, userId);
        task.setTotalCount(totalCount);

        exportTaskMapper.insert(task);
        log.info("创建导出任务: taskId={}, userId={}, totalCount={}", task.getId(), userId, totalCount);

        return task.getId();
    }

    @Override
    @Async("exportTaskExecutor")
    public void asyncExportEmployee(Long taskId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("导出任务不存在: taskId={}", taskId);
            return;
        }

        // 如果任务有历史文件，先删除（重试场景）
        if (task.getFilePath() != null) {
            deletePhysicalFile(task.getFilePath());
        }

        // 更新状态为执行中
        task.setStatus(ExportTask.STATUS_RUNNING);
        exportTaskMapper.updateById(task);

        try {
            // 解析参数
            EmployeeExportDTO exportDTO = objectMapper.readValue(task.getFilterParams(), EmployeeExportDTO.class);
            List<String> fields = objectMapper.readValue(task.getExportFields(), new TypeReference<List<String>>() {});

            // 设置当前用户上下文（用于数据权限）
            SysUser user = new SysUser();
            user.setId(task.getUserId());
            BaseContext.setCurrentUser(user);

            // 设置数据权限条件
            setDataScopeWrapper(user);

            // 执行导出
            String filePath = doExport(exportDTO, fields, task.getFileName());

            // 获取文件大小
            File file = new File(filePath);
            long fileSize = file.length();

            // 更新任务状态为成功
            task.setStatus(ExportTask.STATUS_SUCCESS);
            task.setFilePath(filePath);
            task.setFileSize(fileSize);
            task.setFinishTime(LocalDateTime.now());
            exportTaskMapper.updateById(task);

            // 发送消息通知
            String msgContent = String.format("员工报表导出完成！共%d条记录，文件大小%s",
                    task.getTotalCount(), formatFileSize(fileSize));
            messageService.batchSendMessage(List.of(task.getUserId()), "报表导出完成", msgContent, 2);

            log.info("导出任务完成: taskId={}, filePath={}, size={}",
                    taskId, filePath, formatFileSize(fileSize));

        } catch (Exception e) {
            log.error("导出任务失败: taskId={}", taskId, e);
            task.setStatus(ExportTask.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            exportTaskMapper.updateById(task);

            // 失败时删除临时文件
            deletePhysicalFile(task.getFilePath());

            // 发送失败通知
            messageService.batchSendMessage(List.of(task.getUserId()), "报表导出失败",
                    "导出失败: " + e.getMessage(), 2);
        } finally {
            BaseContext.clear();
            DataScopeContext.clear();
        }
    }

    @Override
    public List<ExportTask> getUserExportTasks(Long userId) {
        return exportTaskMapper.selectByUserId(userId);
    }

    @Override
    public ExportTask getExportTask(Long taskId) {
        return exportTaskMapper.selectById(taskId);
    }

    @Override
    public String getTaskFilePath(Long taskId, Long userId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new IllegalArgumentException("无权访问此任务");
        }
        if (task.getStatus() != ExportTask.STATUS_SUCCESS) {
            throw new IllegalArgumentException("任务尚未完成");
        }
        return task.getFilePath();
    }

    @Override
    public List<EmployeeExportDTO.FieldOption> getFieldOptions() {
        return EmployeeExportDTO.ALL_FIELD_OPTIONS;
    }

    /**
     * 估算总记录数
     */
    private int estimateTotalCount(EmployeeExportDTO exportDTO, Long userId) {
        if (exportDTO.getExportScope() == EmployeeExportDTO.SCOPE_CURRENT_PAGE) {
            return Math.min(exportDTO.getSize() != null ? exportDTO.getSize() : 10, 100);
        }

        QueryWrapper<Employee> wrapper = buildQueryWrapper(exportDTO);
        return Math.toIntExact(employeeMapper.selectCount(wrapper));
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<Employee> buildQueryWrapper(EmployeeExportDTO exportDTO) {
        QueryWrapper<Employee> wrapper = DataScopeContext.getWrapper();
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        // 添加筛选条件
        if (StringUtils.hasText(exportDTO.getName())) {
            wrapper.like("name", exportDTO.getName());
        }
        if (exportDTO.getDeptId() != null) {
            wrapper.eq("dept_id", exportDTO.getDeptId());
        }
        if (exportDTO.getStatus() != null) {
            wrapper.eq("status", exportDTO.getStatus());
        }

        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    /**
     * 设置数据权限条件
     */
    private void setDataScopeWrapper(SysUser user) {
        if (user == null || user.getRoleId() == null) {
            return;
        }

        Long roleId = user.getRoleId();
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();

        // 管理员：全部权限
        if (roleId == 1) {
            DataScopeContext.setWrapper(wrapper);
            return;
        }

        // 部门CEO：本部门及子部门
        if (roleId == 2 && user.getManagedDeptId() != null) {
            List<Long> deptIds = getDeptAndChildrenIds(user.getManagedDeptId());
            wrapper.in("dept_id", deptIds);
            DataScopeContext.setWrapper(wrapper);
            return;
        }

        // 部门经理：本部门
        if (roleId == 3 && user.getManagedDeptId() != null) {
            wrapper.eq("dept_id", user.getManagedDeptId());
            DataScopeContext.setWrapper(wrapper);
            return;
        }

        // 普通员工：只能访问自己创建的员工
        if (roleId == 4) {
            wrapper.eq("create_by", user.getId());
            DataScopeContext.setWrapper(wrapper);
        }
    }

    /**
     * 获取部门及其所有子部门的ID列表
     */
    private List<Long> getDeptAndChildrenIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);
        result.addAll(getChildrenDeptIds(deptId));
        return result;
    }

    /**
     * 递归获取子部门ID
     */
    private List<Long> getChildrenDeptIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId).eq("is_deleted", 0);
        List<Department> children = departmentMapper.selectList(wrapper);

        for (Department child : children) {
            result.add(child.getId());
            result.addAll(getChildrenDeptIds(child.getId()));
        }
        return result;
    }

    /**
     * 执行导出
     */
    private String doExport(EmployeeExportDTO exportDTO, List<String> fields, String fileName) throws IOException {
        // 确保导出目录存在
        Path exportDir = Paths.get(exportPath);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        String filePath = exportDir.resolve(fileName).toString();

        // 检查数据量
        int totalCount = estimateTotalCount(exportDTO, null);
        if (totalCount == 0) {
            throw new RuntimeException("无数据可导出");
        }
        if (totalCount > MAX_EXPORT_COUNT) {
            throw new RuntimeException("导出数据量过大（" + totalCount + "条），请使用筛选条件缩小范围（最多支持" + MAX_EXPORT_COUNT + "条）");
        }

        // 获取部门名称映射
        Map<Long, String> deptNameMap = getDeptNameMap();

        // 创建ExcelWriter - 使用try-finally确保资源释放
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(filePath, EmployeeExportVO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("员工列表").build();

            // 分页导出
            if (exportDTO.getExportScope() == EmployeeExportDTO.SCOPE_CURRENT_PAGE) {
                // 导出当前页
                int page = exportDTO.getPage() != null ? exportDTO.getPage() : 1;
                int size = exportDTO.getSize() != null ? exportDTO.getSize() : 10;
                List<EmployeeExportVO> list = fetchAndConvert(page, size, exportDTO, deptNameMap);
                excelWriter.write(list, writeSheet);
            } else {
                // 导出所有筛选数据（分页读取）
                int totalPage = (int) Math.ceil((double) totalCount / BATCH_SIZE);
                for (int page = 1; page <= totalPage; page++) {
                    List<EmployeeExportVO> list = fetchAndConvert(page, BATCH_SIZE, exportDTO, deptNameMap);
                    excelWriter.write(list, writeSheet);
                    log.debug("导出进度: page={}/{}, size={}", page, totalPage, list.size());
                }
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
        return filePath;
    }

    /**
     * 获取部门名称映射
     */
    private Map<Long, String> getDeptNameMap() {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        List<Department> departments = departmentMapper.selectList(wrapper);
        return departments.stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
    }

    /**
     * 分页获取并转换数据
     */
    private List<EmployeeExportVO> fetchAndConvert(int page, int size, EmployeeExportDTO exportDTO,
                                                     Map<Long, String> deptNameMap) {
        QueryWrapper<Employee> wrapper = buildQueryWrapper(exportDTO);
        Page<Employee> pageParam = new Page<>(page, size);
        Page<Employee> pageData = employeeMapper.selectPage(pageParam, wrapper);

        return pageData.getRecords().stream()
                .map(emp -> convertToVO(emp, deptNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换为导出VO
     */
    private EmployeeExportVO convertToVO(Employee emp, Map<Long, String> deptNameMap) {
        EmployeeExportVO vo = new EmployeeExportVO();
        BeanUtils.copyProperties(emp, vo);
        vo.setDeptName(deptNameMap.getOrDefault(emp.getDeptId(), ""));
        return vo;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2fGB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 删除物理文件
     */
    private void deletePhysicalFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("删除导出文件成功: {}", filePath);
                } else {
                    log.warn("删除导出文件失败: {}", filePath);
                }
            }
        } catch (Exception e) {
            log.error("删除导出文件异常: {}", filePath, e);
        }
    }
}
