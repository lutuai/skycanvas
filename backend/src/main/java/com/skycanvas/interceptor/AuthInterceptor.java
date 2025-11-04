package com.skycanvas.interceptor;

import com.skycanvas.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Token
        String token = request.getHeader(header);
        if (token == null || !token.startsWith(prefix)) {
            log.warn("Token缺失或格式错误");
            response.setStatus(401);
            return false;
        }

        // 移除前缀
        token = token.substring(prefix.length()).trim();

        // 验证Token
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token验证失败");
            response.setStatus(401);
            return false;
        }

        // 获取用户ID并设置到请求属性中
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }
}

