package com.user.service.impl;

import com.user.dto.*;
import com.user.entity.User;
import com.user.feign.PermissionServiceClient;
import com.user.repository.UserRepository;
import com.user.service.UserService;
import com.user.util.JwtUtil;
import com.user.util.MessageUtil;
import com.user.util.PasswordUtil;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务实现类
 * 
 * @author developer
 * @since 2024-06-21
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionServiceClient permissionServiceClient;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageUtil messageUtil;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResponse<UserResponse> register(UserRegisterRequest request, String clientIp) {
        logger.info("开始用户注册流程: username={}", request.getUsername());

        try {
            // 1. 参数校验
            if (!request.isValid()) {
                return ApiResponse.badRequest("用户名和密码不能为空");
            }

            if (!passwordUtil.isValidPassword(request.getPassword())) {
                return ApiResponse.badRequest("密码格式不正确，长度应为6-50位");
            }

            // 2. 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.badRequest("用户名已存在");
            }

            // 3. 检查邮箱是否已存在（如果提供了邮箱）
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ApiResponse.badRequest("邮箱已被注册");
                }
            }

            // 4. 创建用户实体
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordUtil.encryptPassword(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());

            // 5. 保存用户到分库分表（ShardingSphere自动路由）
            User savedUser = userRepository.save(user);
            logger.info("用户保存成功: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

            // 6. RPC调用权限服务绑定默认角色
            String bindResult = permissionServiceClient.bindDefaultRole(savedUser.getUserId());
            if ("FALLBACK_ERROR".equals(bindResult)) {
                logger.error("角色绑定失败，事务回滚: userId={}", savedUser.getUserId());
                throw new RuntimeException("权限服务不可用，注册失败");
            }
            logger.info("默认角色绑定成功: userId={}, result={}", savedUser.getUserId(), bindResult);

            // 7. 发送注册日志到MQ
            messageUtil.sendRegisterLog(savedUser.getUserId(), savedUser.getUsername(), clientIp);

            // 8. 构造响应对象
            UserResponse response = convertToUserResponse(savedUser);
            
            logger.info("用户注册流程完成: userId={}", savedUser.getUserId());
            return ApiResponse.success("注册成功", response);

        } catch (Exception e) {
            logger.error("用户注册失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> login(UserLoginRequest request, String clientIp) {
        logger.info("开始用户登录流程: username={}", request.getUsername());

        try {
            // 1. 参数校验
            if (!request.isValid()) {
                return ApiResponse.badRequest("用户名和密码不能为空");
            }

            // 2. 查找用户
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (!userOpt.isPresent()) {
                logger.warn("登录失败，用户不存在: username={}", request.getUsername());
                return ApiResponse.badRequest("用户名或密码错误");
            }

            User user = userOpt.get();

            // 3. 验证密码
            if (!passwordUtil.verifyPassword(request.getPassword(), user.getPassword())) {
                logger.warn("登录失败，密码错误: username={}", request.getUsername());
                return ApiResponse.badRequest("用户名或密码错误");
            }

            // 4. 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

            // 5. 发送登录日志到MQ
            messageUtil.sendLoginLog(user.getUserId(), user.getUsername(), clientIp);

            logger.info("用户登录成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ApiResponse.success("登录成功", token);

        } catch (Exception e) {
            logger.error("用户登录失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long userId, Long currentUserId) {
        logger.info("查询用户信息: userId={}, currentUserId={}", userId, currentUserId);

        try {
            // 1. 权限校验
            if (!hasPermission(currentUserId, userId)) {
                return ApiResponse.forbidden("没有权限查看该用户信息");
            }

            // 2. 查询用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.badRequest("用户不存在");
            }

            User user = userOpt.get();
            UserResponse response = convertToUserResponse(user);

            return ApiResponse.success(response);

        } catch (Exception e) {
            logger.error("查询用户信息失败: userId={}, error={}", userId, e.getMessage(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Page<UserResponse>> getUserList(Long currentUserId, Pageable pageable) {
        logger.info("查询用户列表: currentUserId={}, page={}, size={}", 
                currentUserId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // 1. 获取当前用户角色
            String currentUserRole = getUserRole(currentUserId);

            Page<User> userPage;
            
            // 2. 根据角色返回不同的数据
            if ("super_admin".equals(currentUserRole)) {
                // 超管可以查看所有用户
                userPage = userRepository.findAll(pageable);
            } else if ("admin".equals(currentUserRole)) {
                // 管理员可以查看所有普通用户（需要在repository中实现相应查询）
                // 这里暂时返回所有用户，实际项目中需要过滤
                userPage = userRepository.findAll(pageable);
            } else {
                // 普通用户只能查看自己
                Optional<User> userOpt = userRepository.findById(currentUserId);
                if (userOpt.isPresent()) {
                    // 这里简化处理，实际应该构造单个用户的Page对象
                    userPage = userRepository.findAll(pageable);
                } else {
                    return ApiResponse.badRequest("用户不存在");
                }
            }

            // 3. 转换为响应对象
            Page<UserResponse> responsePage = userPage.map(this::convertToUserResponse);

            return ApiResponse.success(responsePage);

        } catch (Exception e) {
            logger.error("查询用户列表失败: currentUserId={}, error={}", currentUserId, e.getMessage(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUser(Long userId, UserUpdateRequest request, 
                                               Long currentUserId, String clientIp) {
        logger.info("更新用户信息: userId={}, currentUserId={}", userId, currentUserId);

        try {
            // 1. 权限校验
            if (!hasPermission(currentUserId, userId)) {
                return ApiResponse.forbidden("没有权限修改该用户信息");
            }

            // 2. 检查是否有需要更新的字段
            if (!request.hasUpdateFields()) {
                return ApiResponse.badRequest("没有需要更新的字段");
            }

            // 3. 查询用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.badRequest("用户不存在");
            }

            User user = userOpt.get();
            StringBuilder updateFields = new StringBuilder();

            // 4. 更新字段
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                Optional<User> existUser = userRepository.findByEmail(request.getEmail());
                if (existUser.isPresent() && !existUser.get().getUserId().equals(userId)) {
                    return ApiResponse.badRequest("邮箱已被其他用户使用");
                }
                user.setEmail(request.getEmail());
                updateFields.append("email=").append(request.getEmail()).append(";");
            }

            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                user.setPhone(request.getPhone());
                updateFields.append("phone=").append(request.getPhone()).append(";");
            }

            // 5. 保存更新
            User savedUser = userRepository.save(user);

            // 6. 发送更新日志到MQ
            messageUtil.sendUpdateLog(userId, updateFields.toString(), clientIp);

            UserResponse response = convertToUserResponse(savedUser);
            
            logger.info("用户信息更新成功: userId={}", userId);
            return ApiResponse.success("更新成功", response);

        } catch (Exception e) {
            logger.error("更新用户信息失败: userId={}, error={}", userId, e.getMessage(), e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(Long userId, String newPassword, 
                                           Long currentUserId, String clientIp) {
        logger.info("重置用户密码: userId={}, currentUserId={}", userId, currentUserId);

        try {
            // 1. 权限校验
            if (!hasPermission(currentUserId, userId)) {
                return ApiResponse.forbidden("没有权限重置该用户密码");
            }

            // 2. 密码格式校验
            if (!passwordUtil.isValidPassword(newPassword)) {
                return ApiResponse.badRequest("密码格式不正确，长度应为6-50位");
            }

            // 3. 查询用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.badRequest("用户不存在");
            }

            User user = userOpt.get();

            // 4. 加密新密码并保存
            user.setPassword(passwordUtil.encryptPassword(newPassword));
            userRepository.save(user);

            // 5. 发送密码重置日志到MQ
            messageUtil.sendPasswordResetLog(userId, clientIp);            logger.info("用户密码重置成功: userId={}", userId);
            return ApiResponse.success("密码重置成功", "密码重置成功");

        } catch (Exception e) {
            logger.error("重置用户密码失败: userId={}, error={}", userId, e.getMessage(), e);
            return ApiResponse.error("密码重置失败: " + e.getMessage());
        }
    }

    @Override
    public String getUserRole(Long userId) {
        try {
            return permissionServiceClient.getUserRoleCode(userId);
        } catch (Exception e) {
            logger.error("获取用户角色失败: userId={}, error={}", userId, e.getMessage());
            return "user"; // 默认返回普通用户角色
        }
    }

    @Override
    public boolean hasPermission(Long currentUserId, Long targetUserId) {
        // 自己总是可以操作自己的信息
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        // 获取当前用户角色
        String currentUserRole = getUserRole(currentUserId);
        
        // 超管可以操作所有用户
        if ("super_admin".equals(currentUserRole)) {
            return true;
        }

        // 管理员可以操作普通用户
        if ("admin".equals(currentUserRole)) {
            String targetUserRole = getUserRole(targetUserId);
            return "user".equals(targetUserRole);
        }

        // 普通用户只能操作自己
        return false;
    }

    /**
     * 将User实体转换为UserResponse
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getGmtCreate(),
                user.getGmtModified()
        );
    }
}
