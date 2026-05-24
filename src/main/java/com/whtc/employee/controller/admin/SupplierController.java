package com.whtc.employee.controller.admin;

import com.whtc.employee.common.PageResult;
import com.whtc.employee.common.Result;
import com.whtc.employee.dto.SupplierDTO;
import com.whtc.employee.dto.SupplierPageQueryDTO;
import com.whtc.employee.service.SupplierService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/supplier")
@Slf4j
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 分页查询供应商列表
     */
    @GetMapping("/page")
    public Result<PageResult> page(@Valid SupplierPageQueryDTO supplierPageQueryDTO) {
        log.info("供应商分页查询，参数：{}", supplierPageQueryDTO);
        PageResult pageResult = supplierService.pageQuery(supplierPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询供应商
     */
    @GetMapping("/{id}")
    public Result<SupplierDTO> getById(@PathVariable Long id) {
        log.info("根据id查询供应商，id：{}", id);
        return Result.success(supplierService.getById(id));
    }

    /**
     * 新增供应商
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result save(@Valid @RequestBody SupplierDTO supplierDTO) {
        log.info("新增供应商，数据：{}", supplierDTO);
        supplierService.save(supplierDTO);
        return Result.success();
    }

    /**
     * 更新供应商
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@Valid @RequestBody SupplierDTO supplierDTO) {
        log.info("编辑供应商，数据：{}", supplierDTO);
        supplierService.update(supplierDTO);
        return Result.success();
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        log.info("删除供应商，id：{}", id);
        supplierService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除供应商
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteBatch(@RequestParam @jakarta.validation.constraints.NotEmpty List<Long> ids) {
        log.info("批量删除供应商，ids：{}", ids);
        supplierService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 获取所有供应商列表（用于下拉选择）
     */
    @GetMapping("/list")
    public Result<List<SupplierDTO>> listAll() {
        log.info("获取所有供应商列表");
        List<SupplierDTO> list = supplierService.listAll();
        return Result.success(list);
    }
}
