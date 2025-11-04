package com.skycanvas.controller;

import com.skycanvas.common.Result;
import com.skycanvas.dto.LoginRequest;
import com.skycanvas.dto.UserInfoDTO;
import com.skycanvas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    public Result<UserInfoDTO> login(@RequestBody @Validated LoginRequest request) {
        log.info("用户登录: code={}", request.getCode());
        UserInfoDTO userInfo = userService.login(request);
        return Result.success(userInfo);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserInfoDTO> getUserInfo(@RequestAttribute("userId") Long userId) {
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}

