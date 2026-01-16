package com.scnai.plant.service;

import com.scnai.plant.dto.LoginRequest;
import com.scnai.plant.dto.LoginResponse;
import com.scnai.plant.entity.User;
import com.scnai.plant.repository.UserRepository;
import com.scnai.plant.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务类 - 简化版（无密码加密）
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录（简化版：直接比较明文密码）
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求（用于获取IP）
     * @return 登录响应（包含Token和用户信息）
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 1. 查询用户是否存在
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 直接比较明文密码
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成JWT Token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                request.getRemember()
        );

        // 4. 更新登录信息
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(httpRequest));
        userRepository.save(user);

        // 5. 构造响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());
        userInfo.setEmail(user.getEmail());
        userInfo.setRealName(user.getRealName());
        response.setUser(userInfo);

        return response;
    }

    /**
     * 验证Token
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public LoginResponse.UserInfo verifyToken(String token) {
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }

        // 获取用户ID
        Integer userId = jwtUtil.getUserIdFromToken(token);

        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 构造用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());

        return userInfo;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
