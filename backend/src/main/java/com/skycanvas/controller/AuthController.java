package com.skycanvas.controller;

import com.skycanvas.common.Result;
import com.skycanvas.context.UserContextHolder;
import com.skycanvas.dto.LoginRequest;
import com.skycanvas.dto.PhoneBindRequest;
import com.skycanvas.dto.UserInfoDTO;
import com.skycanvas.dto.UserUpdateRequest;
import com.skycanvas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
     * 用户登录（支持微信小程序和H5）
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
    public Result<UserInfoDTO> getUserInfo() {
        Long userId = UserContextHolder.requireUserId();
        log.info("获取用户信息: userId={}", userId);
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/userinfo")
    public Result<Void> updateUserInfo(@RequestBody @Validated UserUpdateRequest request) {
        Long userId = UserContextHolder.requireUserId();
        log.info("更新用户信息: userId={}", userId);
        userService.updateUserInfo(userId, request);
        return Result.success();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(
            @RequestParam @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        log.info("发送短信验证码: phone={}", phone);
        userService.sendSmsCode(phone);
        return Result.success();
    }
    
    /**
     * 手机号验证码登录
     */
    @PostMapping("/login/phone")
    public Result<UserInfoDTO> loginByPhone(@RequestBody @Validated PhoneBindRequest request) {
        log.info("手机号验证码登录: phone={}", request.getPhone());
        UserInfoDTO userInfo = userService.loginByPhone(request);
        return Result.success(userInfo);
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/phone/bind")
    public Result<Void> bindPhone(@RequestBody @Validated PhoneBindRequest request) {
        Long userId = UserContextHolder.requireUserId();
        log.info("绑定手机号: userId={}", userId);
        userService.bindPhone(userId, request);
        return Result.success();
    }

    /**
     * 刷新Token
     */
    @PostMapping("/token/refresh")
    public Result<UserInfoDTO> refreshToken() {
        Long userId = UserContextHolder.requireUserId();
        log.info("刷新Token: userId={}", userId);
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}

