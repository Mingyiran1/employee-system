package com.whtc.employee.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whtc.employee.common.Result;
import com.whtc.employee.constant.JwtClaimsConstant;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.properties.JwtProperties;
import com.whtc.employee.service.SysUserService;
import com.whtc.employee.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Token黑名单前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    // 错误码常量
    private static final int CODE_TOKEN_MISSING = 401001;
    private static final int CODE_TOKEN_EXPIRED = 401002;
    private static final int CODE_TOKEN_INVALID = 401003;
    private static final int CODE_TOKEN_SIGNATURE_ERROR = 401004;
    private static final int CODE_TOKEN_UNSUPPORTED = 401005;
    private static final int CODE_TOKEN_BLACKLISTED = 401006;

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

            // 检查Token是否为空
            if (token == null || token.trim().isEmpty()) {
                log.warn("Token为空");
                return writeErrorResponse(response, 401, CODE_TOKEN_MISSING, "Token不能为空");
            }

            // 检查Token是否在黑名单中（用户已登出）
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.warn("Token已被加入黑名单");
                return writeErrorResponse(response, 401, CODE_TOKEN_BLACKLISTED, "Token已失效，请重新登录");
            }

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
        } catch (ExpiredJwtException ex) {
            log.error("Token已过期：{}", ex.getMessage());
            return writeErrorResponse(response, 401, CODE_TOKEN_EXPIRED, "Token已过期，请重新登录");
        } catch (SignatureException ex) {
            log.error("Token签名验证失败：{}", ex.getMessage());
            return writeErrorResponse(response, 401, CODE_TOKEN_SIGNATURE_ERROR, "Token签名验证失败");
        } catch (MalformedJwtException ex) {
            log.error("Token格式错误：{}", ex.getMessage());
            return writeErrorResponse(response, 401, CODE_TOKEN_INVALID, "Token格式错误");
        } catch (UnsupportedJwtException ex) {
            log.error("不支持的Token：{}", ex.getMessage());
            return writeErrorResponse(response, 401, CODE_TOKEN_UNSUPPORTED, "不支持的Token格式");
        } catch (Exception ex) {
            log.error("Token验证失败：{}", ex.getMessage());
            return writeErrorResponse(response, 401, CODE_TOKEN_INVALID, "Token验证失败");
        }
    }

    /**
     * 写入错误响应
     */
    private boolean writeErrorResponse(HttpServletResponse response, int httpStatus, int code, String message) throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.error(message);
        // 设置错误码（如果Result类支持）
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getOutputStream().write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return false;
    }
}
