package com.user.service;

import com.user.dto.*;
import com.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 * 
 * @author developer
 * @since 2024-06-21
 */
public interface UserService {

    /**
     * 用户注册
     * 包含分布式事务：用户创建 + 角色绑定
     * 
     * @param request 注册请求
     * @param clientIp 客户端IP
     * @return 注册响应
     */
    ApiResponse<UserResponse> register(UserRegisterRequest request, String clientIp);

    /**
     * 用户登录
     * 验证用户名密码，生成JWT令牌
     * 
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应（包含token）
     */
    ApiResponse<String> login(UserLoginRequest request, String clientIp);

    /**
     * 根据用户ID查询用户信息
     * 需要进行权限校验
     * 
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID
     * @return 用户信息
     */
    ApiResponse<UserResponse> getUserById(Long userId, Long currentUserId);

    /**
     * 分页查询用户列表
     * 根据当前用户权限返回不同的数据
     * 
     * @param currentUserId 当前登录用户ID
     * @param pageable 分页参数
     * @return 用户列表
     */
    ApiResponse<Page<UserResponse>> getUserList(Long currentUserId, Pageable pageable);

    /**
     * 更新用户信息
     * 需要进行权限校验
     * 
     * @param userId 要更新的用户ID
     * @param request 更新请求
     * @param currentUserId 当前登录用户ID
     * @param clientIp 客户端IP
     * @return 更新结果
     */
    ApiResponse<UserResponse> updateUser(Long userId, UserUpdateRequest request, 
                                        Long currentUserId, String clientIp);

    /**
     * 重置用户密码
     * 需要进行权限校验
     * 
     * @param userId 要重置密码的用户ID
     * @param newPassword 新密码
     * @param currentUserId 当前登录用户ID
     * @param clientIp 客户端IP
     * @return 重置结果
     */
    ApiResponse<String> resetPassword(Long userId, String newPassword, 
                                     Long currentUserId, String clientIp);

    /**
     * 检查用户权限
     * 通过RPC调用权限服务获取用户角色
     * 
     * @param userId 用户ID
     * @return 用户角色代码（user/admin/super_admin）
     */
    String getUserRole(Long userId);

    /**
     * 验证用户是否有权限操作目标用户
     * 
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return true表示有权限
     */
    boolean hasPermission(Long currentUserId, Long targetUserId);
}
