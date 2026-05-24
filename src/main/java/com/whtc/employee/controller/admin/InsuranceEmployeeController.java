package com.whtc.employee.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.common.Result;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.InsuranceEmployeeDTO;
import com.whtc.employee.dto.InsuranceEmployeeImportDTO;
import com.whtc.employee.dto.InsuranceEmployeePageQueryDTO;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.listener.InsuranceEmployeeImportListener;
import com.whtc.employee.mapper.InsuredCompanyMapper;
import com.whtc.employee.mapper.PremiumConfigMapper;
import com.whtc.employee.mapper.SupplierMapper;
import com.whtc.employee.service.InsuranceEmployeeService;
import com.whtc.employee.vo.InsuranceEmployeeVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/insurance-employee")
@Slf4j
public class InsuranceEmployeeController {

    @Autowired
    private InsuranceEmployeeService insuranceEmployeeService;

    // Mapper用于创建导入监听器
    @Autowired
    private com.whtc.employee.mapper.InsuranceEmployeeMapper insuranceEmployeeMapper;

    @Autowired
    private InsuredCompanyMapper insuredCompanyMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private PremiumConfigMapper premiumConfigMapper;

    /**
     * 保险员工分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(@Valid InsuranceEmployeePageQueryDTO queryDTO) {
        log.info("保险员工分页查询，参数：{}", queryDTO);
        PageResult pageResult = insuranceEmployeeService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询保险员工
     */
    @GetMapping("/{id}")
    public Result<InsuranceEmployeeVO> getById(@PathVariable Long id) {
        log.info("根据id查询保险员工，id：{}", id);
        InsuranceEmployeeVO employee = insuranceEmployeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    /**
     * 新增保险员工
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result save(@Valid @RequestBody InsuranceEmployeeDTO employeeDTO) {
        log.info("新增保险员工，员工数据：{}", employeeDTO);
        insuranceEmployeeService.saveEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 更新保险员工
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@Valid @RequestBody InsuranceEmployeeDTO employeeDTO) {
        log.info("编辑保险员工信息，员工数据：{}", employeeDTO);
        insuranceEmployeeService.updateEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 删除保险员工
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        log.info("删除保险员工，id：{}", id);
        insuranceEmployeeService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除保险员工
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteBatch(@RequestParam @jakarta.validation.constraints.NotEmpty List<Long> ids) {
        log.info("批量删除保险员工，ids：{}", ids);
        insuranceEmployeeService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 获取所有保险员工列表
     */
    @GetMapping("/list-all")
    public Result<List<InsuranceEmployeeVO>> listAll(InsuranceEmployeePageQueryDTO queryDTO) {
        log.info("获取所有保险员工列表，参数：{}", queryDTO);
        List<InsuranceEmployeeVO> list = insuranceEmployeeService.listAll(queryDTO);
        return Result.success(list);
    }

    /**
     * 导出保险员工Excel
     */
    @GetMapping("/export")
    public void exportExcel(InsuranceEmployeePageQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        log.info("导出保险员工Excel，参数：{}", queryDTO);

        List<InsuranceEmployeeVO> list = insuranceEmployeeService.listAll(queryDTO);
        List<InsuranceEmployeeExportRow> exportRows = list.stream()
                .map(this::toExportRow)
                .toList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("保险员工导出_" + LocalDate.now(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), InsuranceEmployeeExportRow.class)
                .sheet("保险员工")
                .doWrite(exportRows);
    }

    /**
     * Excel导入保险员工
     * 仅管理员可执行导入操作
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam MultipartFile file) throws IOException {
        // 校验文件大小（最大10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(400, "文件大小超过限制（最大10MB）");
        }

        // 校验文件格式
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error(400, "请上传Excel文件（.xlsx或.xls格式）");
        }

        log.info("导入保险员工数据，文件名：{}，大小：{}字节", filename, file.getSize());

        // 每次导入创建新的Listener实例，避免状态复用问题
        InsuranceEmployeeImportListener importListener = new InsuranceEmployeeImportListener(
                insuranceEmployeeMapper,
                insuredCompanyMapper,
                supplierMapper,
                premiumConfigMapper
        );

        // 解析Excel
        EasyExcel.read(file.getInputStream(), InsuranceEmployeeImportDTO.class, importListener).sheet().doRead();

        // 获取解析成功的数据
        List<com.whtc.employee.entity.InsuranceEmployee> successData = importListener.getAllData();

        // 如果有成功的数据，调用Service进行批量插入（带事务控制）
        if (!successData.isEmpty()) {
            Map<String, Object> importResult = insuranceEmployeeService.importBatch(successData);

            // 合并结果
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", importListener.getSuccessCount());
            result.put("errorCount", importListener.getErrorCount());
            result.put("errorMessages", importListener.getErrorMessages());

            if (importListener.getErrorCount() > 0) {
                return Result.error(400, "导入完成，但有" + importListener.getErrorCount() + "条数据导入失败", result);
            }
            return Result.success(result);
        } else {
            // 全部失败
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", 0);
            result.put("errorCount", importListener.getErrorCount());
            result.put("errorMessages", importListener.getErrorMessages());
            return Result.error(400, "导入失败，所有数据都有错误", result);
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        log.info("下载保险员工导入模板");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("保险员工导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<InsuranceEmployeeImportDTO> templateList = new ArrayList<>();
        InsuranceEmployeeImportDTO example = new InsuranceEmployeeImportDTO();
        example.setName("张三");
        example.setIdCard("110101199001011234"); // 示例身份证（使用标准格式）
        example.setPhone("13800138000");
        example.setEmail("zhangsan@example.com");
        example.setCompanyName("A公司");
        example.setSupplierName("供应商A");
        example.setJobType("一类");
        example.setHireDate(LocalDate.now());
        example.setRemark("示例数据，请删除后填写");
        templateList.add(example);

        EasyExcel.write(response.getOutputStream(), InsuranceEmployeeImportDTO.class).sheet("保险员工").doWrite(templateList);
    }

    private InsuranceEmployeeExportRow toExportRow(InsuranceEmployeeVO employee) {
        SysUser currentUser = BaseContext.getCurrentUser();
        boolean isAdmin = currentUser != null
                && (Long.valueOf(1L).equals(currentUser.getRoleId()) || "admin".equals(currentUser.getRoleCode()));

        InsuranceEmployeeExportRow row = new InsuranceEmployeeExportRow();
        row.setName(isAdmin ? employee.getName() : maskName(employee.getName()));
        row.setIdCard(isAdmin ? employee.getIdCard() : maskIdCard(employee.getIdCard()));
        row.setPhone(isAdmin ? employee.getPhone() : maskPhone(employee.getPhone()));
        row.setEmail(isAdmin ? employee.getEmail() : maskEmail(employee.getEmail()));
        row.setCompanyName(employee.getCompanyName());
        row.setSupplierName(employee.getSupplierName());
        row.setJobType(employee.getJobType());
        row.setAnnualPremium(employee.getAnnualPremium() == null ? null : employee.getAnnualPremium().toPlainString());
        row.setDailyPremium(employee.getDailyPremium() == null ? null : employee.getDailyPremium().toPlainString());
        row.setRealTimePremium(employee.getRealTimePremium() == null ? null : employee.getRealTimePremium().toPlainString());
        row.setHireDate(employee.getHireDate() == null ? null : employee.getHireDate().toString());
        row.setLeaveDate(employee.getLeaveDate() == null ? null : employee.getLeaveDate().toString());
        row.setStatus(employee.getStatus() == null ? null : (employee.getStatus() == 1 ? "在职" : "离职"));
        row.setRemark(employee.getRemark());
        return row;
    }

    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return "**" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + "******" + idCard.substring(12);
        }
        return idCard;
    }

    @lombok.Data
    private static class InsuranceEmployeeExportRow {
        @ExcelProperty(value = "姓名", index = 0)
        private String name;

        @ExcelProperty(value = "身份证号", index = 1)
        private String idCard;

        @ExcelProperty(value = "手机号", index = 2)
        private String phone;

        @ExcelProperty(value = "邮箱", index = 3)
        private String email;

        @ExcelProperty(value = "投保公司", index = 4)
        private String companyName;

        @ExcelProperty(value = "供应商", index = 5)
        private String supplierName;

        @ExcelProperty(value = "工种", index = 6)
        private String jobType;

        @ExcelProperty(value = "年保费", index = 7)
        private String annualPremium;

        @ExcelProperty(value = "保费标准(天)", index = 8)
        private String dailyPremium;

        @ExcelProperty(value = "实时保费", index = 9)
        private String realTimePremium;

        @ExcelProperty(value = "入职时间", index = 10)
        private String hireDate;

        @ExcelProperty(value = "离职时间", index = 11)
        private String leaveDate;

        @ExcelProperty(value = "状态", index = 12)
        private String status;

        @ExcelProperty(value = "备注", index = 13)
        private String remark;
    }
}
