package com.scnai.plant.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Integer id;
        private String username;
        private String role;
        private String email;
        private String realName;
    }
}
