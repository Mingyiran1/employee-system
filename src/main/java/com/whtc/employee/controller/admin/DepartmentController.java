package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.entity.Department;
import com.whtc.employee.service.DepartmentService;
import com.whtc.employee.vo.DeptTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 最大部门层级限制：5层
 */
@RestController
@RequestMapping("/admin/department")
@Slf4j
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 最大部门层级深度
     */
    private static final int MAX_DEPT_LEVEL = 5;

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
        // 校验部门层级
        if (department.getParentId() != null) {
            int parentLevel = departmentService.getDeptLevel(department.getParentId());
            if (parentLevel + 1 > MAX_DEPT_LEVEL) {
                return Result.error("部门层级不能超过" + MAX_DEPT_LEVEL + "层");
            }
        }
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
        departmentService.deleteById(id);
        return Result.success();
    }

    /**
     * 获取部门树形结构（用于组织架构图）
     */
    @GetMapping("/tree")
    public Result<List<DeptTreeVO>> getDeptTree() {
        log.info("获取部门树形结构");
        List<DeptTreeVO> tree = departmentService.getDeptTree();
        return Result.success(tree);
    }
}
