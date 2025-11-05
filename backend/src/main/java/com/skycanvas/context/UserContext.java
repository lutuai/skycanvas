package com.skycanvas.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户上下文信息
 * 存储当前请求的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名（可选，后续扩展使用）
     */
    private String username;
    
    /**
     * 用户角色（可选，后续扩展使用）
     */
    private String role;
    
    /**
     * 简化构造函数：只需要userId
     */
    public UserContext(Long userId) {
        this.userId = userId;
    }
}

