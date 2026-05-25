package com.whtc.employee.dto;

import com.whtc.employee.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 保险员工DTO
 */
@Data
public class InsuranceEmployeeDTO {

    private Long id;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = RegexConstant.ID_CARD, message = RegexConstant.ID_CARD_MESSAGE)
    private String idCard;

    @Pattern(regexp = RegexConstant.PHONE, message = RegexConstant.PHONE_MESSAGE)
    private String phone;

    @Pattern(regexp = RegexConstant.EMAIL, message = RegexConstant.EMAIL_MESSAGE)
    private String email;

    /**
     * 工种: 销售/客服/理赔/管理/司机
     */
    private String jobType;

    /**
     * 所属投保公司ID
     */
    private Long companyId;

    /**
     * 所属供应商ID（劳务公司）
     */
    private Long supplierId;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 状态: 1=在职, 2=离职
     */
    private Integer status;

    /**
     * 离职日期
     */
    private LocalDate leaveDate;

    /**
     * 备注
     */
    private String remark;
}
