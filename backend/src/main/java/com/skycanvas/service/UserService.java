package com.skycanvas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skycanvas.dto.LoginRequest;
import com.skycanvas.dto.UserInfoDTO;
import com.skycanvas.entity.User;
import com.skycanvas.mapper.UserMapper;
import com.skycanvas.utils.JwtUtils;
import com.skycanvas.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    /**
     * 微信小程序登录
     */
    public UserInfoDTO login(LoginRequest request) {
        // 1. 调用微信接口获取openid
        String openid = getOpenidByCode(request.getCode());
        
        // 2. 查询或创建用户
        User user = getUserByOpenid(openid);
        if (user == null) {
            user = createUser(openid, request.getNickname(), request.getAvatar());
        } else {
            // 更新用户信息
            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }
            if (request.getAvatar() != null) {
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
            throw new RuntimeException("用户不存在");
        }

        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
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
            throw new RuntimeException("微信登录失败");
        } catch (Exception e) {
            log.error("调用微信API失败", e);
            throw new RuntimeException("微信登录失败: " + e.getMessage());
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
        user.setCredits(0);  // 初始积分为0
        user.setTotalVideos(0);
        user.setStatus(0);
        user.setDeleted(0);
        
        userMapper.insert(user);
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
        }
    }
}

