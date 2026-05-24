package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投保公司实体类
 * 存储所有需要为员工购买保险的公司（A、B、C、D等）
 */
@Data
@TableName("insured_company")
public class InsuredCompany {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 公司代码
     */
    private String code;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 公司地址
     */
    private String address;

    /**
     * 状态: 1=启用, 0=禁用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
