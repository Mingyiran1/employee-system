package com.whtc.employee.dto;

import com.whtc.employee.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @Pattern(regexp = RegexConstant.PHONE, message = RegexConstant.PHONE_MESSAGE)
    private String phone;

    @Pattern(regexp = RegexConstant.EMAIL, message = RegexConstant.EMAIL_MESSAGE)
    private String email;

    @Pattern(regexp = RegexConstant.ID_CARD, message = RegexConstant.ID_CARD_MESSAGE)
    private String idCard;

    private Long deptId;

    private String position;

    private LocalDate entryDate;

    private Integer status;

    private String address;
}
