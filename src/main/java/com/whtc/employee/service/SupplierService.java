package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.dto.SupplierDTO;
import com.whtc.employee.dto.SupplierPageQueryDTO;
import com.whtc.employee.entity.Supplier;

import java.util.List;

public interface SupplierService extends IService<Supplier> {

    /**
     * 供应商分页查询
     */
    PageResult pageQuery(SupplierPageQueryDTO supplierPageQueryDTO);

    /**
     * 新增供应商
     */
    void save(SupplierDTO supplierDTO);

    /**
     * 更新供应商
     */
    void update(SupplierDTO supplierDTO);

    /**
     * 根据ID查询供应商
     */
    SupplierDTO getById(Long id);

    /**
     * 根据ID删除供应商
     */
    void deleteById(Long id);

    /**
     * 批量删除供应商
     */
    void deleteByIds(List<Long> ids);

    /**
     * 获取所有供应商列表
     */
    List<SupplierDTO> listAll();
}
