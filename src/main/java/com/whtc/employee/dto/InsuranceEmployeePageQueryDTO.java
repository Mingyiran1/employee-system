package com.whtc.employee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 保险员工分页查询DTO
 */
@Data
public class InsuranceEmployeePageQueryDTO implements Serializable {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;

    /**
     * 员工姓名（模糊查询）
     */
    private String name;

    /**
     * 身份证号（模糊查询）
     */
    private String idCard;

    /**
     * 投保公司ID
     */
    private Long companyId;

    /**
     * 投保公司名称（模糊查询）
     */
    private String companyName;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（模糊查询）
     */
    private String supplierName;

    /**
     * 工种
     */
    private String jobType;

    /**
     * 状态: 1=在职, 2=离职
     */
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDateEnd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDateEnd;
}
