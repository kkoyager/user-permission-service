package com.user.service;

import com.user.dto.ApiResponse;
import com.user.dto.UserLoginRequest;
import com.user.dto.UserRegisterRequest;
import com.user.dto.UserResponse;
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

        testUser = new User();
        testUser.setUserId(1001L);
        testUser.setUsername("testuser");
        testUser.setPassword("encrypted_password");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
    }

    @Test
    void testRegisterSuccess() {
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

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("注册成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());

        // 验证方法调用
        verify(userRepository).save(any(User.class));
        verify(permissionServiceClient).bindDefaultRole(anyLong());
        verify(messageUtil).sendRegisterLog(anyLong(), anyString(), anyString());
    }

    @Test
    void testRegisterWithExistingUsername() {
        // Mock用户名已存在
        when(passwordUtil.isValidPassword(anyString())).thenReturn(true);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // 执行测试
        ApiResponse<UserResponse> response = userService.register(registerRequest, "127.0.0.1");

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("用户名已存在", response.getMessage());
        assertNull(response.getData());

        // 验证没有保存用户
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // Mock依赖方法
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn("mock_jwt_token");
        doNothing().when(messageUtil).sendLoginLog(anyLong(), anyString(), anyString());

        // 执行测试
        ApiResponse<String> response = userService.login(loginRequest, "127.0.0.1");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("登录成功", response.getMessage());
        assertEquals("mock_jwt_token", response.getData());

        // 验证方法调用
        verify(jwtUtil).generateToken(testUser.getUserId(), testUser.getUsername());
        verify(messageUtil).sendLoginLog(anyLong(), anyString(), anyString());
    }

    @Test
    void testLoginWithWrongPassword() {
        // Mock依赖方法
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(false);

        // 执行测试
        ApiResponse<String> response = userService.login(loginRequest, "127.0.0.1");

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
}
