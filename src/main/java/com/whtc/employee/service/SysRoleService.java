package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 获取所有角色列表
     */
    List<SysRole> getAllRoles();

    /**
     * 根据角色编码获取角色
     */
    SysRole getByCode(String code);

    /**
     * 根据ID删除角色（带缓存清除）
     */
    boolean removeRoleById(Long id);
}
