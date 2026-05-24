package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保费配置实体类
 * 按工种配置保费费率
 */
@Data
@TableName("premium_config")
public class PremiumConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工种: 一类/二类/三类
     */
    private String jobType;

    /**
     * 保费费率(如0.015=1.5%)
     */
    private BigDecimal rate;

    /**
     * 基数(年薪基数，元)
     */
    private BigDecimal baseSalary;

    /**
     * 年保费(自动计算: base_salary * rate)
     */
    private BigDecimal annualPremium;

    /**
     * 状态: 1=启用, 0=禁用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
