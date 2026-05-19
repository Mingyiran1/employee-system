package com.whtc.employee.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SupplierPageQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer size = 10;

    private String name;

    private Integer cooperationStatus;
}
