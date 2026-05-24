package com.whtc.employee.config;

import com.whtc.employee.context.BaseContext;
import com.whtc.employee.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义缓存Key生成器
 * 用于生成带数据权限标识的缓存key
 */
@Component("dataScopeKeyGenerator")
@Slf4j
public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder key = new StringBuilder();

        // 基础key：类名+方法名
        key.append(target.getClass().getSimpleName())
           .append(":")
           .append(method.getName());

        // 添加参数
        for (Object param : params) {
            key.append(":").append(param != null ? param.toString() : "null");
        }

        // 添加数据权限标识
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser != null && currentUser.getRoleId() != null) {
            Integer dataScope = switch (currentUser.getRoleId().intValue()) {
                case 1 -> 1; // admin - 全部
                case 2 -> 2; // dept_CEO - 本部门及以下
                case 3 -> 3; // dept_manager - 本部门
                case 4 -> 4; // user - 仅本人
                default -> 4;
            };

            key.append(":user_").append(currentUser.getId())
               .append(":scope_").append(dataScope);
        }

        String finalKey = key.toString();
        log.debug("生成缓存Key: {}", finalKey);
        return finalKey;
    }
}
