package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.whtc.employee.annotation.DataMasking;
import com.whtc.employee.enums.MaskingType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保险员工实体类
 * 需要购买保险的员工（来自各投保公司）
 */
@Data
@TableName("insurance_employee")
public class InsuranceEmployee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工姓名
     */
    @DataMasking(MaskingType.NAME)
    private String name;

    /**
     * 身份证号
     */
    @DataMasking(MaskingType.ID_CARD)
    private String idCard;

    /**
     * 手机号码
     */
    @DataMasking(MaskingType.PHONE)
    private String phone;

    /**
     * 电子邮箱
     */
    @DataMasking(MaskingType.EMAIL)
    private String email;

    /**
     * 所属投保公司ID（insured_company.id）
     */
    private Long companyId;

    /**
     * 所属供应商ID（supplier.id，劳务公司）
     */
    private Long supplierId;

    /**
     * 工种: 销售/客服/理赔/管理/司机
     */
    private String jobType;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 离职日期
     */
    private LocalDate leaveDate;

    /**
     * 状态: 1=在职, 2=离职
     */
    private Integer status;

    /**
     * 年保费金额（根据工种费率自动计算）
     * 计算公式: base_salary * rate
     */
    private BigDecimal annualPremium;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    // ==================== 非数据库字段 ====================

    /**
     * 投保公司名称
     */
    @TableField(exist = false)
    private String companyName;

    /**
     * 供应商名称
     */
    @TableField(exist = false)
    private String supplierName;

    /**
     * 保费费率（从premium_config获取）
     */
    @TableField(exist = false)
    private BigDecimal premiumRate;

    /**
     * 计算基数（从premium_config获取）
     */
    @TableField(exist = false)
    private BigDecimal baseSalary;
}
