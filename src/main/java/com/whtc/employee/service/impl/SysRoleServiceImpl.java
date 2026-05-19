package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.entity.SysRole;
import com.whtc.employee.mapper.SysRoleMapper;
import com.whtc.employee.service.SysRoleService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    @Cacheable(value = "role", key = "'all'")
    public List<SysRole> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getId);
        return this.list(wrapper);
    }

    @Override
    public SysRole getByCode(String code) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, code);
        return this.getOne(wrapper);
    }
}
