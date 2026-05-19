package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.entity.Department;
import com.whtc.employee.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/department")
@Slf4j
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取所有部门列表
     */
    @GetMapping("/list")
    public Result<List<Department>> list() {
        log.info("获取所有部门列表");
        List<Department> list = departmentService.getAllDepartments();
        return Result.success(list);
    }

    /**
     * 新增部门
     */
    @PostMapping
    public Result save(@RequestBody Department department) {
        log.info("新增部门：{}", department);
        departmentService.save(department);
        return Result.success();
    }

    /**
     * 更新部门
     */
    @PutMapping
    public Result update(@RequestBody Department department) {
        log.info("编辑部门：{}", department);
        departmentService.updateById(department);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除部门，id：{}", id);
        departmentService.removeById(id);
        return Result.success();
    }
}
