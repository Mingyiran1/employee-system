package com.whtc.employee.context;

import com.whtc.employee.entity.SysUser;

/**
 * 用户上下文工具类
 * 基于ThreadLocal存储当前登录用户信息
 */
public class BaseContext {

    /**
     * 存储当前用户ID
     */
    private static final ThreadLocal<Long> threadLocalUserId = new ThreadLocal<>();

    /**
     * 存储当前用户信息（包含角色）
     */
    private static final ThreadLocal<SysUser> threadLocalUser = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        threadLocalUserId.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return threadLocalUserId.get();
    }

    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(SysUser user) {
        threadLocalUser.set(user);
    }

    /**
     * 获取当前用户信息
     */
    public static SysUser getCurrentUser() {
        return threadLocalUser.get();
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        threadLocalUserId.remove();
        threadLocalUser.remove();
    }
}
