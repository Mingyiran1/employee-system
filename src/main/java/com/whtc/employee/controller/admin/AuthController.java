package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.constant.JwtClaimsConstant;
import com.whtc.employee.dto.LoginDTO;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.properties.JwtProperties;
import com.whtc.employee.service.SysUserService;
import com.whtc.employee.utils.JwtUtil;
import com.whtc.employee.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/auth")
@Slf4j
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Token黑名单前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);
        SysUser user = sysUserService.login(loginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getId());
        claims.put(JwtClaimsConstant.USER_NAME, user.getUsername());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        LoginVO loginVO = LoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .name(user.getRealName())
                .token(token)
                .roleId(user.getRoleId())
                .roleCode(user.getRoleCode())
                .build();

        return Result.success(loginVO);
    }

    /**
     * 登出 - 将Token加入黑名单使其失效
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 从请求头中获取Token
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        if (token != null && !token.isEmpty()) {
            // 计算Token剩余有效时间
            long ttlMillis = jwtProperties.getAdminTtl();

            // 将Token加入黑名单，设置过期时间为Token剩余有效期
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, 1, ttlMillis, TimeUnit.MILLISECONDS);
            log.info("用户登出，Token已加入黑名单");
        }

        return Result.success();
    }
}
