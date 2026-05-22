package com.whtc.employee.dto;

import lombok.Data;

import java.util.List;

/**
 * 员工导出请求DTO
 */
@Data
public class EmployeeExportDTO {

    /**
     * 导出范围：1-当前页 2-所有筛选数据
     */
    private Integer exportScope;

    /**
     * 当前页码（exportScope=1时使用）
     */
    private Integer page;

    /**
     * 每页大小（exportScope=1时使用）
     */
     private Integer size;

    /**
     * 筛选条件（exportScope=2时使用）
     */
    private String name;
    private Long deptId;
    private Integer status;

    /**
     * 要导出的字段列表
     */
    private List<String> fields;

    // 导出范围常量
    public static final int SCOPE_CURRENT_PAGE = 1;
    public static final int SCOPE_ALL_FILTERED = 2;

    /**
     * 默认导出字段
     */
    public static final List<String> DEFAULT_FIELDS = List.of(
            "name", "gender", "phone", "email", "deptName", "position", "entryDate", "status"
    );

    /**
     * 所有可选字段
     */
    public static final List<FieldOption> ALL_FIELD_OPTIONS = List.of(
            new FieldOption("name", "姓名", true),
            new FieldOption("gender", "性别", true),
            new FieldOption("phone", "手机号", true),
            new FieldOption("email", "邮箱", true),
            new FieldOption("deptName", "部门", true),
            new FieldOption("position", "职位", true),
            new FieldOption("entryDate", "入职日期", true),
            new FieldOption("status", "状态", true),
            new FieldOption("idCard", "身份证号", false),
            new FieldOption("address", "家庭住址", false)
    );

    /**
     * 字段选项
     */
    @Data
    public static class FieldOption {
        private String value;
        private String label;
        private Boolean defaultChecked;

        public FieldOption(String value, String label, Boolean defaultChecked) {
            this.value = value;
            this.label = label;
            this.defaultChecked = defaultChecked;
        }
    }
}
