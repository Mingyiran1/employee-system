package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.constant.MessageConstant;
import com.whtc.employee.dto.LoginDTO;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.exception.LoginFailedException;
import com.whtc.employee.mapper.SysUserMapper;
import com.whtc.employee.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 登录失败次数限制配置
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final String LOGIN_LOCK_KEY_PREFIX = "login:lock:";
    private static final String LOGIN_ATTEMPT_KEY_PREFIX = "login:attempt:";

    @Override
    public SysUser login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String lockKey = LOGIN_LOCK_KEY_PREFIX + username;
        String attemptKey = LOGIN_ATTEMPT_KEY_PREFIX + username;

        // 检查账号是否被锁定
        Boolean isLocked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(isLocked)) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_LOCKED);
        }

        SysUser user = sysUserMapper.selectByUsername(username);

        // 统一错误消息，防止用户枚举攻击
        String errorMessage = MessageConstant.LOGIN_FAILED;

        if (user == null) {
            // 账号不存在，记录失败次数
            recordFailedAttempt(attemptKey, lockKey);
            throw new LoginFailedException(errorMessage);
        }

        // 密码比对 - 支持明文和BCrypt加密两种格式（测试环境兼容，生产环境应强制BCrypt）
        String inputPassword = loginDTO.getPassword();
        String storedPassword = user.getPassword();

        boolean passwordMatch;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // BCrypt加密密码
            passwordMatch = passwordEncoder.matches(inputPassword, storedPassword);
        } else {
            // 明文密码（仅用于测试环境兼容）
            log.warn("用户 {} 使用明文密码登录，建议尽快更新为BCrypt加密", username);
            passwordMatch = inputPassword.equals(storedPassword);
        }

        if (!passwordMatch) {
            // 密码错误，记录失败次数
            recordFailedAttempt(attemptKey, lockKey);
            throw new LoginFailedException(errorMessage);
        }

        // 登录成功，清除失败记录
        redisTemplate.delete(attemptKey);

        // 清空密码后返回
        user.setPassword(null);
        return user;
    }

    /**
     * 记录登录失败次数，超过限制则锁定账号
     */
    private void recordFailedAttempt(String attemptKey, String lockKey) {
        // 获取当前失败次数
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptKey);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            // 超过限制，锁定账号15分钟
            redisTemplate.opsForValue().set(lockKey, 1, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
        } else {
            // 增加失败次数，5分钟后过期
            redisTemplate.opsForValue().set(attemptKey, attempts, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public Long countByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRoleId, roleId);
        return this.count(wrapper);
    }
}
