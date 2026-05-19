package com.whtc.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeVO {

    private Long id;
    private String name;
    private Integer gender;
    private String phone;
    private String email;
    private String idCard;
    private Long deptId;
    private String deptName;
    private String position;
    private LocalDate entryDate;
    private Integer status;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
