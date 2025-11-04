package com.skycanvas.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skycanvas.common.Result;
import com.skycanvas.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证拦截器
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
            log.warn("Token缺失或格式错误");
            writeErrorResponse(response, "未授权，请先登录");
            return false;
        }

        // 移除前缀
        token = token.substring(prefix.length()).trim();

        // 验证Token
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token验证失败");
            writeErrorResponse(response, "Token已过期或无效，请重新登录");
            return false;
        }

        // 获取用户ID并设置到请求属性中
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
        } catch (Exception e) {
            log.error("解析Token失败", e);
            writeErrorResponse(response, "Token解析失败，请重新登录");
            return false;
        }

        return true;
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

