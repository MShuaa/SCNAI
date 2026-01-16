package com.scnai.plant.controller;

import com.scnai.plant.common.ApiResponse;
import com.scnai.plant.dto.LoginRequest;
import com.scnai.plant.dto.LoginResponse;
import com.scnai.plant.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 处理用户登录、登出、验证Token等
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含Token和用户信息）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.login(request, httpRequest);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "INVALID_CREDENTIALS");
        }
    }

    /**
     * 验证Token
     *
     * @param request HTTP请求（从Header中获取Token）
     * @return 用户信息
     */
    @GetMapping("/verify")
    public ApiResponse<LoginResponse.UserInfo> verify(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ApiResponse.error("未提供认证令牌", "TOKEN_MISSING");
            }

            String token = authHeader.substring(7);
            LoginResponse.UserInfo userInfo = authService.verifyToken(token);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "TOKEN_INVALID");
        }
    }

    /**
     * 用户登出
     *
     * @return 成功消息
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // 简单实现：前端清除Token即可
        // 完整实现可以将Token加入黑名单（需要Redis）
        return ApiResponse.success("登出成功");
    }
}
