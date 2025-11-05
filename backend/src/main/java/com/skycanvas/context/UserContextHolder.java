package com.skycanvas.context;

/**
 * 用户上下文持有者
 * 使用ThreadLocal存储当前请求的用户信息
 * 
 * 设计说明：
 * 1. ThreadLocal 保证了线程安全性，每个请求线程都有独立的用户上下文
 * 2. 拦截器负责在请求开始时设置用户上下文，请求结束时清除
 * 3. 业务代码中可以随时通过静态方法获取当前用户信息
 * 
 * @author SkyCanvas Team
 */
public class UserContextHolder {
    
    /**
     * ThreadLocal存储用户上下文
     * InheritableThreadLocal 支持父子线程传递（如果使用异步任务需要）
     */
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置用户上下文
     * 
     * @param userContext 用户上下文
     */
    public static void setContext(UserContext userContext) {
        CONTEXT_HOLDER.set(userContext);
    }
    
    /**
     * 设置用户ID（简化方法）
     * 
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        CONTEXT_HOLDER.set(new UserContext(userId));
    }
    
    /**
     * 获取用户上下文
     * 
     * @return 用户上下文，如果未设置则返回null
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未设置则返回null
     */
    public static Long getUserId() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * 获取当前用户ID（必须存在，否则抛出异常）
     * 建议在必须要用户登录的业务方法中使用
     * 
     * @return 用户ID
     * @throws IllegalStateException 如果用户未登录
     */
    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录或用户上下文未设置");
        }
        return userId;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未设置则返回null
     */
    public static String getUsername() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getUsername() : null;
    }
    
    /**
     * 获取当前用户角色
     * 
     * @return 用户角色，如果未设置则返回null
     */
    public static String getRole() {
        UserContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getRole() : null;
    }
    
    /**
     * 清除用户上下文
     * ⚠️ 重要：必须在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 检查是否已登录
     * 
     * @return true-已登录，false-未登录
     */
    public static boolean isAuthenticated() {
        return getUserId() != null;
    }
}

