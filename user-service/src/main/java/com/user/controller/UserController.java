package com.user.controller;

import com.user.dto.*;
import com.user.service.UserService;
import com.user.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 * 提供用户相关的REST API接口
 * 
 * @author developer
 * @since 2024-06-21
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册接口
     * POST /user/register
     */
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody UserRegisterRequest request,
                                            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        logger.info("收到用户注册请求: username={}, ip={}", request.getUsername(), clientIp);
        
        return userService.register(request, clientIp);
    }

    /**
     * 用户登录接口
     * POST /user/login
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody UserLoginRequest request,
                                   HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        logger.info("收到用户登录请求: username={}, ip={}", request.getUsername(), clientIp);
        
        return userService.login(request, clientIp);
    }

    /**
     * 查询用户信息接口
     * GET /user/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserInfo(@PathVariable Long userId,
                                               HttpServletRequest httpRequest) {
        // 从请求头中获取token并解析当前用户ID
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        logger.info("查询用户信息: userId={}, currentUserId={}", userId, currentUserId);
        return userService.getUserById(userId, currentUserId);
    }

    /**
     * 分页查询用户列表接口
     * GET /users?page=0&size=10
     */
    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> getUserList(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     HttpServletRequest httpRequest) {
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        Pageable pageable = PageRequest.of(page, size);
        logger.info("查询用户列表: currentUserId={}, page={}, size={}", currentUserId, page, size);
        
        return userService.getUserList(currentUserId, pageable);
    }

    /**
     * 更新用户信息接口
     * PUT /user/{userId}
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId,
                                              @RequestBody UserUpdateRequest request,
                                              HttpServletRequest httpRequest) {
        // 获取当前用户ID和IP
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        String clientIp = getClientIp(httpRequest);
        logger.info("更新用户信息: userId={}, currentUserId={}, ip={}", userId, currentUserId, clientIp);
        
        return userService.updateUser(userId, request, currentUserId, clientIp);
    }

    /**
     * 重置密码接口
     * POST /user/reset-password
     */
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request,
                                           HttpServletRequest httpRequest) {
        // 获取当前用户ID和IP
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        String clientIp = getClientIp(httpRequest);
        logger.info("重置用户密码: targetUserId={}, currentUserId={}, ip={}", 
                request.getUserId(), currentUserId, clientIp);
        
        return userService.resetPassword(request.getUserId(), request.getNewPassword(), 
                                       currentUserId, clientIp);
    }

    /**
     * 获取当前用户角色接口（用于前端权限控制）
     * GET /user/current-role
     */
    @GetMapping("/current-role")
    public ApiResponse<String> getCurrentUserRole(HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        String role = userService.getUserRole(currentUserId);
        logger.info("获取用户角色: userId={}, role={}", currentUserId, role);
        
        return ApiResponse.success("获取角色成功", role);
    }    /**
     * 健康检查接口
     * GET /user/health
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("用户服务运行正常", "用户服务运行正常");
    }

    /**
     * 从HTTP请求中提取当前用户ID
     * 通过解析Authorization头中的JWT令牌获取
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7); // 去掉"Bearer "前缀
            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.warn("解析用户令牌失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑代理和负载均衡的情况
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // 多级代理的情况，取第一个IP
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 密码重置请求DTO
     * 内部类，用于接收重置密码的请求参数
     */
    public static class ResetPasswordRequest {
        private Long userId;
        private String newPassword;

        public ResetPasswordRequest() {
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        @Override
        public String toString() {
            return "ResetPasswordRequest{" +
                    "userId=" + userId +
                    '}';
        }
    }
}
