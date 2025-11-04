package com.skycanvas.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 积分余额
     */
    private Integer credits;

    /**
     * 总生成视频数
     */
    private Integer totalVideos;

    /**
     * JWT Token
     */
    private String token;
}

