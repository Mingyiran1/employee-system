package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.dto.SupplierDTO;
import com.whtc.employee.dto.SupplierPageQueryDTO;
import com.whtc.employee.entity.Supplier;
import com.whtc.employee.mapper.SupplierMapper;
import com.whtc.employee.service.SupplierService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public PageResult pageQuery(SupplierPageQueryDTO supplierPageQueryDTO) {
        Page<Supplier> pageInfo = new Page<>(supplierPageQueryDTO.getPage(), supplierPageQueryDTO.getSize());
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(supplierPageQueryDTO.getName())) {
            wrapper.like(Supplier::getName, supplierPageQueryDTO.getName());
        }
        if (supplierPageQueryDTO.getCooperationStatus() != null) {
            wrapper.eq(Supplier::getCooperationStatus, supplierPageQueryDTO.getCooperationStatus());
        }

        wrapper.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> pageData = this.page(pageInfo, wrapper);

        return new PageResult(pageData.getTotal(), pageData.getRecords());
    }

    @Override
    public void save(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        if (supplier.getCooperationStatus() == null) {
            supplier.setCooperationStatus(1);
        }
        this.save(supplier);
    }

    @Override
    public void update(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        this.updateById(supplier);
    }

    @Override
    public SupplierDTO getById(Long id) {
        Supplier supplier = super.getById(id);
        if (supplier == null) {
            return null;
        }
        SupplierDTO dto = new SupplierDTO();
        BeanUtils.copyProperties(supplier, dto);
        return dto;
    }

    @Override
    public void deleteById(Long id) {
        this.removeById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        this.removeByIds(ids);
    }
}
