package com.whtc.employee.vo;

import com.whtc.employee.annotation.DataMasking;
import com.whtc.employee.enums.MaskingType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeVO {

    private Long id;

    @DataMasking(MaskingType.NAME)
    private String name;

    private Integer gender;

    @DataMasking(MaskingType.PHONE)
    private String phone;

    @DataMasking(MaskingType.EMAIL)
    private String email;

    @DataMasking(MaskingType.ID_CARD)
    private String idCard;

    private Long deptId;
    private String deptName;
    private String position;
    private LocalDate entryDate;
    private Integer status;

    @DataMasking(MaskingType.ADDRESS)
    private String address;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
