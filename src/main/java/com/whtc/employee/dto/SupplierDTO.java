package com.whtc.employee.dto;

import com.whtc.employee.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 100, message = "供应商名称长度不能超过100个字符")
    private String name;

    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    private String contactName;

    @Pattern(regexp = RegexConstant.PHONE, message = RegexConstant.PHONE_MESSAGE)
    private String contactPhone;

    @Pattern(regexp = RegexConstant.EMAIL, message = RegexConstant.EMAIL_MESSAGE)
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    @Size(max = 500, message = "业务范围长度不能超过500个字符")
    private String businessScope;

    private Integer cooperationStatus;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
