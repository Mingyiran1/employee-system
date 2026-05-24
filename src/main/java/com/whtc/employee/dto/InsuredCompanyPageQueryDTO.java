package com.whtc.employee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 投保公司分页查询DTO
 */
@Data
public class InsuredCompanyPageQueryDTO implements Serializable {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;

    /**
     * 公司名称（模糊查询）
     */
    private String name;

    /**
     * 状态: 1=启用, 0=禁用
     */
    private Integer status;
}
