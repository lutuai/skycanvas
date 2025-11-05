package com.skycanvas.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skycanvas.common.Result;
import com.skycanvas.context.UserContextHolder;
import com.skycanvas.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证拦截器
 * 
 * 职责：
 * 1. 验证请求中的JWT Token
 * 2. 解析Token获取用户信息
 * 3. 将用户信息设置到ThreadLocal上下文中
 * 4. 请求结束后清除上下文（防止内存泄漏）
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Token
        String token = request.getHeader(header);
        if (token == null || !token.startsWith(prefix)) {
            log.warn("Token缺失或格式错误，请求路径: {}", request.getRequestURI());
            writeErrorResponse(response, "未授权，请先登录");
            return false;
        }

        // 移除前缀
        token = token.substring(prefix.length()).trim();

        // 验证Token
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token验证失败，请求路径: {}", request.getRequestURI());
            writeErrorResponse(response, "Token已过期或无效，请重新登录");
            return false;
        }

        // 获取用户ID并设置到ThreadLocal上下文中（而不是RequestAttribute）
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            UserContextHolder.setUserId(userId);
            log.debug("用户认证成功，userId: {}, 请求路径: {}", userId, request.getRequestURI());
        } catch (Exception e) {
            log.error("解析Token失败，请求路径: {}", request.getRequestURI(), e);
            writeErrorResponse(response, "Token解析失败，请重新登录");
            return false;
        }

        return true;
    }

    /**
     * 请求完成后清除用户上下文
     * ⚠️ 重要：防止ThreadLocal内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        UserContextHolder.clear();
        log.debug("清除用户上下文，请求路径: {}", request.getRequestURI());
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        Result<?> result = Result.unauthorized(message);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }
}


