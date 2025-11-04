package com.skycanvas.service;

import com.skycanvas.entity.LoginLog;
import com.skycanvas.mapper.LoginLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录日志服务
 */
@Slf4j
@Service
public class LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    /**
     * 记录登录成功日志
     */
    public void recordLoginSuccess(Long userId) {
        LoginLog loginLog = buildLoginLog(userId);
        loginLog.setStatus(1);
        loginLogMapper.insert(loginLog);
        log.info("记录登录成功日志: userId={}, ip={}", userId, loginLog.getLoginIp());
    }

    /**
     * 记录登录失败日志
     */
    public void recordLoginFail(Long userId, String reason) {
        LoginLog loginLog = buildLoginLog(userId);
        loginLog.setStatus(0);
        loginLog.setFailReason(reason);
        loginLogMapper.insert(loginLog);
        log.info("记录登录失败日志: userId={}, reason={}", userId, reason);
    }

    /**
     * 构建登录日志对象
     */
    private LoginLog buildLoginLog(Long userId) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);

        // 获取请求信息
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 获取IP地址
                String ip = getIpAddress(request);
                loginLog.setLoginIp(ip);
                
                // 获取User-Agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    loginLog.setDeviceType(parseDeviceType(userAgent));
                    loginLog.setBrowser(parseBrowser(userAgent));
                    loginLog.setOs(parseOs(userAgent));
                }
            }
        } catch (Exception e) {
            log.error("获取请求信息失败", e);
        }

        return loginLog;
    }

    /**
     * 获取真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 解析设备类型
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {
            return "WeChat";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac";
        }
        return "Other";
    }

    /**
     * 解析浏览器
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {
            return "WeChat";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari")) {
            return "Safari";
        } else if (userAgent.contains("edge")) {
            return "Edge";
        }
        return "Other";
    }

    /**
     * 解析操作系统
     */
    private String parseOs(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }
}

