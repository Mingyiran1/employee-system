package com.whtc.employee.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 保险员工Excel导入DTO
 * 用于EasyExcel读取导入数据
 */
@Data
public class InsuranceEmployeeImportDTO {

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

    @ExcelProperty(value = "入职日期", index = 7)
    private LocalDate hireDate;

    @ExcelProperty(value = "备注", index = 8)
    private String remark;
}
