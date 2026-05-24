package com.whtc.employee.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投保公司VO（包含员工数量统计）
 */
@Data
public class InsuredCompanyVO {

    private Long id;

    /**
     * 公司名称
     */
    private String name;

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
     * 员工数量
     */
    private Long employeeCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
