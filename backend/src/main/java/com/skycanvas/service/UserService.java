package com.skycanvas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skycanvas.dto.LoginRequest;
import com.skycanvas.dto.PhoneBindRequest;
import com.skycanvas.dto.UserInfoDTO;
import com.skycanvas.dto.UserUpdateRequest;
import com.skycanvas.entity.User;
import com.skycanvas.exception.BusinessException;
import com.skycanvas.mapper.UserMapper;
import com.skycanvas.utils.JwtUtils;
import com.skycanvas.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoginLogService loginLogService;

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    @Value("${user.register.bonus-credits:100}")
    private Integer registerBonusCredits;

    /**
     * 微信小程序登录
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO login(LoginRequest request) {
        // 1. 调用微信接口获取openid
        String openid = getOpenidByCode(request.getCode());
        
        // 2. 查询或创建用户
        User user = getUserByOpenid(openid);
        boolean isNewUser = false;
        
        if (user == null) {
            user = createUser(openid, request.getNickname(), request.getAvatar());
            isNewUser = true;
        } else {
            // 检查用户状态
            if (user.getStatus() == 1) {
                throw BusinessException.of(403, "账号已被禁用，请联系客服");
            }
            
            // 更新用户信息
            if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (request.getAvatar() != null && !request.getAvatar().equals(user.getAvatar())) {
                user.setAvatar(request.getAvatar());
            }
            userMapper.updateById(user);
        }

        // 3. 生成JWT Token
        String token = jwtUtils.generateToken(user.getId());

        // 4. 缓存用户信息
        cacheUserInfo(user);

        // 5. 构建返回对象
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setToken(token);
        
        // 记录登录日志
        log.info("用户登录成功: userId={}, openid={}, isNewUser={}", user.getId(), openid, isNewUser);
        loginLogService.recordLoginSuccess(user.getId());
        
        return dto;
    }

    /**
     * 获取用户信息
     */
    public UserInfoDTO getUserInfo(Long userId) {
        // 先从缓存获取
        User user = getCachedUser(userId);
        if (user == null) {
            user = userMapper.selectById(userId);
            if (user != null) {
                cacheUserInfo(user);
            }
        }

        if (user == null) {
            throw BusinessException.of("用户不存在");
        }

        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        // 手机号脱敏
        if (user.getPhone() != null) {
            dto.setPhone(maskPhone(user.getPhone()));
        }
        return dto;
    }

    /**
     * 通过code获取openid
     */
    private String getOpenidByCode(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("openid")) {
                return (String) response.get("openid");
            }
            log.error("获取openid失败: {}", response);
            loginLogService.recordLoginFail(null, "获取openid失败");
            throw BusinessException.of("微信登录失败，请重试");
        } catch (Exception e) {
            log.error("调用微信API失败", e);
            loginLogService.recordLoginFail(null, "调用微信API失败: " + e.getMessage());
            throw BusinessException.of("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 根据openid获取用户
     */
    private User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 创建新用户
     */
    private User createUser(String openid, String nickname, String avatar) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(nickname != null ? nickname : "用户" + System.currentTimeMillis());
        user.setAvatar(avatar);
        user.setCredits(registerBonusCredits);  // 新用户注册奖励积分
        user.setTotalVideos(0);
        user.setStatus(0);
        user.setDeleted(0);
        
        userMapper.insert(user);
        log.info("创建新用户: userId={}, openid={}, bonusCredits={}", user.getId(), openid, registerBonusCredits);
        return user;
    }

    /**
     * 缓存用户信息
     */
    private void cacheUserInfo(User user) {
        String key = "user:info:" + user.getId();
        redisUtils.set(key, user, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存的用户信息
     */
    private User getCachedUser(Long userId) {
        String key = "user:info:" + userId;
        return (User) redisUtils.get(key);
    }

    /**
     * 更新用户积分
     */
    public void updateCredits(Long userId, Integer credits) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setCredits(credits);
            userMapper.updateById(user);
            // 清除缓存
            redisUtils.delete("user:info:" + userId);
        }
    }

    /**
     * 增加视频生成次数
     */
    public void incrementTotalVideos(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalVideos(user.getTotalVideos() + 1);
            userMapper.updateById(user);
            // 清除缓存
            redisUtils.delete("user:info:" + userId);
        }
    }

    /**
     * 绑定手机号
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, PhoneBindRequest request) {
        // 1. 验证验证码
        String codeKey = "sms:code:" + request.getPhone();
        String cachedCode = (String) redisUtils.get(codeKey);
        
        if (cachedCode == null) {
            throw BusinessException.of("验证码已过期，请重新获取");
        }
        
        if (!cachedCode.equals(request.getCode())) {
            throw BusinessException.of("验证码错误");
        }

        // 2. 检查手机号是否已被绑定
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        wrapper.ne(User::getId, userId);
        User existUser = userMapper.selectOne(wrapper);
        
        if (existUser != null) {
            throw BusinessException.of("该手机号已被其他账号绑定");
        }

        // 3. 绑定手机号
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        
        user.setPhone(request.getPhone());
        userMapper.updateById(user);

        // 4. 清除验证码和缓存
        redisUtils.delete(codeKey);
        redisUtils.delete("user:info:" + userId);
        
        log.info("用户绑定手机号成功: userId={}, phone={}", userId, request.getPhone());
    }

    /**
     * 发送短信验证码
     */
    public void sendSmsCode(String phone) {
        // 1. 检查发送频率
        String limitKey = "sms:limit:" + phone;
        if (redisUtils.hasKey(limitKey)) {
            throw BusinessException.of("操作过于频繁，请稍后再试");
        }

        // 2. 生成验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 3. TODO: 调用短信服务发送验证码
        // smsService.sendCode(phone, code);
        log.info("发送短信验证码: phone={}, code={}", phone, code);

        // 4. 缓存验证码（5分钟有效）
        String codeKey = "sms:code:" + phone;
        redisUtils.set(codeKey, code, 5, TimeUnit.MINUTES);

        // 5. 设置发送限制（60秒内不能重复发送）
        redisUtils.set(limitKey, "1", 60, TimeUnit.SECONDS);
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }

        boolean updated = false;
        
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            user.setNickname(request.getNickname());
            updated = true;
        }
        
        if (request.getAvatar() != null && !request.getAvatar().equals(user.getAvatar())) {
            user.setAvatar(request.getAvatar());
            updated = true;
        }

        if (updated) {
            userMapper.updateById(user);
            redisUtils.delete("user:info:" + userId);
            log.info("更新用户信息成功: userId={}", userId);
        }
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(Long userId) {
        return userMapper.selectById(userId) != null;
    }

    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

