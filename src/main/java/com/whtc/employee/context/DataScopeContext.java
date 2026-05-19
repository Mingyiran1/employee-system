package com.whtc.employee.context;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 数据权限上下文
 * 使用ThreadLocal传递数据权限过滤条件
 */
public class DataScopeContext {

    private static final ThreadLocal<QueryWrapper<?>> DATA_SCOPE_WRAPPER = new ThreadLocal<>();

    /**
     * 设置数据权限QueryWrapper
     */
    public static void setWrapper(QueryWrapper<?> wrapper) {
        DATA_SCOPE_WRAPPER.set(wrapper);
    }

    /**
     * 获取数据权限QueryWrapper
     */
    @SuppressWarnings("unchecked")
    public static <T> QueryWrapper<T> getWrapper() {
        return (QueryWrapper<T>) DATA_SCOPE_WRAPPER.get();
    }

    /**
     * 清除ThreadLocal
     */
    public static void clear() {
        DATA_SCOPE_WRAPPER.remove();
    }

    /**
     * 检查是否有数据权限条件
     */
    public static boolean hasWrapper() {
        return DATA_SCOPE_WRAPPER.get() != null;
    }
}
