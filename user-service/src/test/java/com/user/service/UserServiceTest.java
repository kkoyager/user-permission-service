package com.user.service;

import com.user.dto.ApiResponse;
import com.user.dto.UserLoginRequest;
import com.user.dto.UserRegisterRequest;
import com.user.dto.UserResponse;
import com.user.dto.UserUpdateRequest;
import com.user.entity.User;
import com.user.feign.PermissionServiceClient;
import com.user.repository.UserRepository;
import com.user.service.impl.UserServiceImpl;
import com.user.util.JwtUtil;
import com.user.util.MessageUtil;
import com.user.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 * 测试用户相关的业务逻辑
 * 
 * @author developer
 * @since 2024-06-21
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionServiceClient permissionServiceClient;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("123456");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("13800138000");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        testUser = new User();        testUser.setUserId(1001L);
        testUser.setUsername("testuser");
        testUser.setPassword("encrypted_password");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
    }

    @Test
    void testRegisterSuccess() {
        System.out.println("\n=== 测试用户注册成功 ===");
        System.out.println("注册请求: " + registerRequest.getUsername() + ", " + registerRequest.getEmail());

        // Mock依赖方法
        when(passwordUtil.isValidPassword(anyString())).thenReturn(true);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordUtil.encryptPassword(anyString())).thenReturn("encrypted_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(permissionServiceClient.bindDefaultRole(anyLong())).thenReturn("SUCCESS");
        doNothing().when(messageUtil).sendRegisterLog(anyLong(), anyString(), anyString());

        // 执行测试
        ApiResponse<UserResponse> response = userService.register(registerRequest, "127.0.0.1");

        // 打印操作结果
        System.out.println("注册结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        if (response.getData() != null) {
            UserResponse user = response.getData();
            System.out.println("创建的用户: ID=" + user.getUserId() + ", 用户名=" + user.getUsername() + 
                             ", 邮箱=" + user.getEmail() + ", 手机=" + user.getPhone());
        }

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("注册成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());

        // 验证方法调用
        verify(userRepository).save(any(User.class));
        verify(permissionServiceClient).bindDefaultRole(anyLong());        verify(messageUtil).sendRegisterLog(anyLong(), anyString(), anyString());
    }

    @Test
    void testRegisterWithExistingUsername() {
        System.out.println("\n=== 测试用户名已存在的注册 ===");
        System.out.println("尝试注册已存在的用户名: " + registerRequest.getUsername());

        // Mock用户名已存在
        when(passwordUtil.isValidPassword(anyString())).thenReturn(true);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // 执行测试
        ApiResponse<UserResponse> response = userService.register(registerRequest, "127.0.0.1");

        // 打印操作结果
        System.out.println("注册结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        System.out.println("数据: " + (response.getData() != null ? "有数据" : "无数据"));

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("用户名已存在", response.getMessage());
        assertNull(response.getData());

        // 验证没有保存用户
        verify(userRepository, never()).save(any(User.class));
    }    @Test
    void testLoginSuccess() {
        System.out.println("\n=== 测试用户登录成功 ===");
        System.out.println("登录请求: 用户名=" + loginRequest.getUsername());

        // Mock依赖方法
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn("mock_jwt_token");
        doNothing().when(messageUtil).sendLoginLog(anyLong(), anyString(), anyString());

        // 执行测试
        ApiResponse<String> response = userService.login(loginRequest, "127.0.0.1");

        // 打印操作结果
        System.out.println("登录结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        System.out.println("生成的Token: " + response.getData());
        System.out.println("登录用户ID: " + testUser.getUserId());

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("登录成功", response.getMessage());
        assertEquals("mock_jwt_token", response.getData());

        // 验证方法调用
        verify(jwtUtil).generateToken(testUser.getUserId(), testUser.getUsername());
        verify(messageUtil).sendLoginLog(anyLong(), anyString(), anyString());
    }    @Test
    void testLoginWithWrongPassword() {
        System.out.println("\n=== 测试密码错误的登录 ===");
        System.out.println("尝试用错误密码登录: " + loginRequest.getUsername());

        // Mock依赖方法
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(false);

        // 执行测试
        ApiResponse<String> response = userService.login(loginRequest, "127.0.0.1");

        // 打印操作结果
        System.out.println("登录结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        System.out.println("Token: " + (response.getData() != null ? response.getData() : "未生成"));

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("用户名或密码错误", response.getMessage());
        assertNull(response.getData());

        // 验证没有生成token
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void testLoginWithNonExistentUser() {
        // Mock用户不存在
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // 执行测试
        ApiResponse<String> response = userService.login(loginRequest, "127.0.0.1");

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("用户名或密码错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testGetUserRole() {
        // Mock权限服务返回角色
        when(permissionServiceClient.getUserRoleCode(anyLong())).thenReturn("admin");

        // 执行测试
        String role = userService.getUserRole(1001L);

        // 验证结果
        assertEquals("admin", role);
        verify(permissionServiceClient).getUserRoleCode(1001L);
    }

    @Test
    void testHasPermission() {
        // Mock当前用户为管理员，目标用户为普通用户
        when(permissionServiceClient.getUserRoleCode(1001L)).thenReturn("admin");
        when(permissionServiceClient.getUserRoleCode(1002L)).thenReturn("user");

        // 测试管理员访问普通用户
        boolean hasPermission = userService.hasPermission(1001L, 1002L);
        assertTrue(hasPermission);

        // 测试用户访问自己
        boolean selfPermission = userService.hasPermission(1002L, 1002L);
        assertTrue(selfPermission);
    }

    @Test
    void testRegisterWithInvalidPassword() {
        // Mock密码格式不正确
        when(passwordUtil.isValidPassword(anyString())).thenReturn(false);

        // 执行测试
        ApiResponse<UserResponse> response = userService.register(registerRequest, "127.0.0.1");

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("密码格式不正确，长度应为6-50位", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testGetUserByIdSuccess() {
        System.out.println("\n=== 测试获取用户信息成功 ===");
        Long targetUserId = 1001L;
        Long currentUserId = 1001L; // 自己查看自己的信息

        // Mock依赖方法
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));

        // 执行测试  
        ApiResponse<UserResponse> response = userService.getUserById(targetUserId, currentUserId);

        // 打印操作结果
        System.out.println("查询用户ID: " + targetUserId + ", 当前用户ID: " + currentUserId);
        System.out.println("查询结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        if (response.getData() != null) {
            UserResponse user = response.getData();
            System.out.println("用户信息: ID=" + user.getUserId() + ", 用户名=" + user.getUsername() + 
                             ", 邮箱=" + user.getEmail() + ", 手机=" + user.getPhone());
        }        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testUser.getUsername(), response.getData().getUsername());
    }    @Test
    void testGetUserByIdNotFound() {
        System.out.println("\n=== 测试获取不存在的用户信息 ===");
        Long targetUserId = 9999L;
        Long currentUserId = 1001L;

        // Mock权限检查：当前用户不是管理员，且不是查看自己
        when(permissionServiceClient.getUserRoleCode(currentUserId)).thenReturn("user");
        // 不需要mock targetUserId的角色，因为权限检查会在查询目标用户角色之前就失败

        // 执行测试
        ApiResponse<UserResponse> response = userService.getUserById(targetUserId, currentUserId);

        // 打印操作结果
        System.out.println("查询用户ID: " + targetUserId + " (不存在)");
        System.out.println("当前用户ID: " + currentUserId + " (普通用户，无权限查看其他用户)");
        System.out.println("查询结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());

        // 验证结果
        assertNotNull(response);
        assertEquals(403, response.getCode());
        assertEquals("没有权限查看该用户信息", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testGetUserByIdNotFoundWithAdminPermission() {
        System.out.println("\n=== 测试管理员查询不存在的用户信息 ===");
        Long targetUserId = 9999L;
        Long currentUserId = 1001L; // 管理员

        // Mock管理员权限，用户不存在
        when(permissionServiceClient.getUserRoleCode(currentUserId)).thenReturn("admin");
        when(permissionServiceClient.getUserRoleCode(targetUserId)).thenReturn("user");
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // 执行测试
        ApiResponse<UserResponse> response = userService.getUserById(targetUserId, currentUserId);

        // 打印操作结果
        System.out.println("查询用户ID: " + targetUserId + " (不存在)");
        System.out.println("当前用户ID: " + currentUserId + " (管理员，有权限查看)");
        System.out.println("查询结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("用户不存在", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testUpdateUserSuccess() {
        System.out.println("\n=== 测试更新用户信息成功 ===");
        Long userId = 1001L;
        Long currentUserId = 1001L; // 自己更新自己的信息

        // 准备更新请求
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPhone("13900139000");

        // 准备更新后的用户对象
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername(testUser.getUsername());
        updatedUser.setPassword(testUser.getPassword());
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setPhone("13900139000");

        // Mock依赖方法
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        doNothing().when(messageUtil).sendUpdateLog(anyLong(), anyString(), anyString());

        // 执行测试
        ApiResponse<UserResponse> response = userService.updateUser(userId, updateRequest, currentUserId, "127.0.0.1");

        // 打印操作结果
        System.out.println("更新用户ID: " + userId);
        System.out.println("更新前邮箱: " + testUser.getEmail() + " -> 更新后邮箱: " + updateRequest.getEmail());
        System.out.println("更新前手机: " + testUser.getPhone() + " -> 更新后手机: " + updateRequest.getPhone());
        System.out.println("更新结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());
        if (response.getData() != null) {
            UserResponse user = response.getData();
            System.out.println("更新后用户信息: 邮箱=" + user.getEmail() + ", 手机=" + user.getPhone());
        }

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("更新成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("newemail@example.com", response.getData().getEmail());
        assertEquals("13900139000", response.getData().getPhone());

        // 验证方法调用
        verify(userRepository).save(any(User.class));
        verify(messageUtil).sendUpdateLog(anyLong(), anyString(), anyString());
    }

    @Test
    void testResetPasswordSuccess() {
        System.out.println("\n=== 测试重置密码成功 ===");
        Long userId = 1002L;
        Long currentUserId = 1001L; // 管理员重置其他用户密码
        String newPassword = "newpassword123";

        // Mock管理员权限
        when(permissionServiceClient.getUserRoleCode(currentUserId)).thenReturn("admin");
        when(permissionServiceClient.getUserRoleCode(userId)).thenReturn("user");
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordUtil.isValidPassword(newPassword)).thenReturn(true);
        when(passwordUtil.encryptPassword(newPassword)).thenReturn("encrypted_new_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(messageUtil).sendPasswordResetLog(anyLong(), anyString());

        // 执行测试
        ApiResponse<String> response = userService.resetPassword(userId, newPassword, currentUserId, "127.0.0.1");

        // 打印操作结果
        System.out.println("管理员ID: " + currentUserId + " 重置用户ID: " + userId + " 的密码");
        System.out.println("新密码: " + newPassword);
        System.out.println("重置结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("密码重置成功", response.getMessage());
        assertEquals("密码重置成功", response.getData());

        // 验证方法调用
        verify(passwordUtil).encryptPassword(newPassword);
        verify(userRepository).save(any(User.class));
        verify(messageUtil).sendPasswordResetLog(anyLong(), anyString());
    }    @Test
    void testGetUserByIdNoPermission() {
        System.out.println("\n=== 测试普通用户无权限查看其他用户信息 ===");
        Long targetUserId = 1002L;
        Long currentUserId = 1003L; // 另一个普通用户

        // Mock当前用户是普通用户（目标用户的角色不需要mock，因为不会被调用）
        when(permissionServiceClient.getUserRoleCode(currentUserId)).thenReturn("user");

        // 执行测试
        ApiResponse<UserResponse> response = userService.getUserById(targetUserId, currentUserId);

        // 打印操作结果
        System.out.println("查询用户ID: " + targetUserId + " (普通用户)");
        System.out.println("当前用户ID: " + currentUserId + " (普通用户，无权限查看其他用户)");
        System.out.println("查询结果: 状态码=" + response.getCode() + ", 消息=" + response.getMessage());

        // 验证结果
        assertNotNull(response);
        assertEquals(403, response.getCode());
        assertEquals("没有权限查看该用户信息", response.getMessage());
        assertNull(response.getData());
    }
}
