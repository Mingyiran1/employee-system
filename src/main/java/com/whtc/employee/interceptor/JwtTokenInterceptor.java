package com.whtc.employee.interceptor;

import com.whtc.employee.constant.JwtClaimsConstant;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.properties.JwtProperties;
import com.whtc.employee.service.SysUserService;
import com.whtc.employee.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final SysUserService sysUserService;

    public JwtTokenInterceptor(JwtProperties jwtProperties, SysUserService sysUserService) {
        this.jwtProperties = jwtProperties;
        this.sysUserService = sysUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前用户id：{}", empId);

            // 将用户ID存入ThreadLocal
            BaseContext.setCurrentUserId(empId);

            // 查询用户信息（包含角色）并存入ThreadLocal
            SysUser user = sysUserService.getById(empId);
            if (user != null) {
                BaseContext.setCurrentUser(user);
                log.info("当前用户角色：{}", user.getRoleCode());
            }

            return true;
        } catch (Exception ex) {
            // 不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
