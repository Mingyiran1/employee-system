package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.entity.SysRole;
import com.whtc.employee.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/admin/role")
@Slf4j
public class RoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 获取所有角色列表
     */
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        log.info("获取所有角色列表");
        List<SysRole> list = sysRoleService.getAllRoles();
        return Result.success(list);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result save(@RequestBody SysRole role) {
        log.info("新增角色：{}", role);
        sysRoleService.save(role);
        return Result.success();
    }

    /**
     * 更新角色
     */
    @PutMapping
    public Result update(@RequestBody SysRole role) {
        log.info("更新角色：{}", role);
        sysRoleService.updateById(role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除角色，id：{}", id);
        sysRoleService.removeById(id);
        return Result.success();
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        log.info("获取角色详情，id：{}", id);
        SysRole role = sysRoleService.getById(id);
        return Result.success(role);
    }
}
