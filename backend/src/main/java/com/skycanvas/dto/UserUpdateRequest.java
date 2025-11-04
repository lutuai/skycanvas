package com.skycanvas.dto;

import lombok.Data;
import javax.validation.constraints.Size;

/**
 * 用户信息更新请求DTO
 */
@Data
public class UserUpdateRequest {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
}

