package com.whtc.employee.dto;

import com.whtc.employee.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    private String contactName;

    @Pattern(regexp = RegexConstant.PHONE, message = RegexConstant.PHONE_MESSAGE)
    private String contactPhone;

    @Pattern(regexp = RegexConstant.EMAIL, message = RegexConstant.EMAIL_MESSAGE)
    private String email;

    private String address;

    private String businessScope;

    private Integer cooperationStatus;

    private String remark;
}
