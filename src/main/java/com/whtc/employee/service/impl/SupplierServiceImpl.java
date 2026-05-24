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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(rollbackFor = Exception.class)
    public void save(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        if (supplier.getCooperationStatus() == null) {
            supplier.setCooperationStatus(1);
        }
        this.save(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        // 校验是否存在关联业务数据
        checkSupplierReferences(id);
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("删除的供应商ID列表不能为空");
        }
        // 逐个校验关联数据
        for (Long id : ids) {
            checkSupplierReferences(id);
        }
        this.removeByIds(ids);
    }

    /**
     * 检查供应商是否存在关联业务数据
     * @param supplierId 供应商ID
     * @throws RuntimeException 如果存在关联数据则抛出异常
     */
    private void checkSupplierReferences(Long supplierId) {
        // TODO: 根据实际业务需求添加关联校验
        // 例如：检查是否存在采购订单、合同等关联数据
        // 当前系统中未发现其他表引用supplier_id，保留此方法作为扩展点

        // 示例：如果有采购订单表，可以添加如下校验：
        // LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        // wrapper.eq(PurchaseOrder::getIsDeleted, 0);
        // long count = purchaseOrderMapper.selectCount(wrapper);
        // if (count > 0) {
        //     throw new RuntimeException("该供应商存在关联采购订单，无法删除");
        // }
    }

    @Override
    public List<SupplierDTO> listAll() {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getIsDeleted, 0)
               .orderByDesc(Supplier::getCreateTime);
        List<Supplier> list = this.list(wrapper);

        return list.stream().map(supplier -> {
            SupplierDTO dto = new SupplierDTO();
            BeanUtils.copyProperties(supplier, dto);
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
