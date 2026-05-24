package com.whtc.employee.vo;

import com.whtc.employee.annotation.DataMasking;
import com.whtc.employee.enums.MaskingType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InsuranceEmployeeVO {

    private Long id;

    @DataMasking(MaskingType.NAME)
    private String name;

    @DataMasking(MaskingType.PHONE)
    private String phone;

    @DataMasking(MaskingType.EMAIL)
    private String email;

    @DataMasking(MaskingType.ID_CARD)
    private String idCard;

    private String jobType;

    private Long companyId;

    private String companyName;

    private Long supplierId;

    private String supplierName;

    private BigDecimal annualPremium;

    private LocalDate hireDate;

    private LocalDate leaveDate;

    private Integer status;

    private String remark;

    private BigDecimal premiumRate;

    private BigDecimal dailyPremium;

    private BigDecimal realTimePremium;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
