package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.dto.LoginDTO;
import com.whtc.employee.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser login(LoginDTO loginDTO);

    /**
     * 根据角色ID统计用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    Long countByRoleId(Long roleId);
}
