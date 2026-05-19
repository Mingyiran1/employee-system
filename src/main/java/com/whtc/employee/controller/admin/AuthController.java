package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.constant.JwtClaimsConstant;
import com.whtc.employee.dto.LoginDTO;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.properties.JwtProperties;
import com.whtc.employee.service.SysUserService;
import com.whtc.employee.utils.JwtUtil;
import com.whtc.employee.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@Slf4j
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtProperties jwtProperties;

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
     * 登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
}
