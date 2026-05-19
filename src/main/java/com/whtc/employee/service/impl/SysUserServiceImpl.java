package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.constant.MessageConstant;
import com.whtc.employee.dto.LoginDTO;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.exception.LoginFailedException;
import com.whtc.employee.mapper.SysUserMapper;
import com.whtc.employee.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser login(LoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 先尝试BCrypt匹配，如果不成功则尝试明文匹配（开发环境方便测试）
        String inputPassword = loginDTO.getPassword();
        String storedPassword = user.getPassword();

        boolean passwordMatch = false;
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            // BCrypt加密密码
            passwordMatch = passwordEncoder.matches(inputPassword, storedPassword);
        } else {
            // 明文密码（开发测试用）
            passwordMatch = inputPassword.equals(storedPassword);
        }

        if (!passwordMatch) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }
        // 清空密码后返回
        user.setPassword(null);
        return user;
    }
}
