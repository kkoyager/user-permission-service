package com.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.dto.*;
import com.user.service.UserService;
import com.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器测试类
 * 测试用户相关的REST API接口
 * 
 * @author developer
 * @since 2024-06-21
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;
    private String validToken;

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

        updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPhone("13900139000");

        userResponse = new UserResponse();
        userResponse.setUserId(1001L);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setPhone("13800138000");

        validToken = "Bearer valid.jwt.token";
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Mock成功注册
        when(userService.register(any(UserRegisterRequest.class), anyString()))
                .thenReturn(ApiResponse.success("注册成功", userResponse));

        // 执行请求并验证
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testRegisterWithInvalidData() throws Exception {
        // Mock注册失败（用户名已存在）
        when(userService.register(any(UserRegisterRequest.class), anyString()))
                .thenReturn(ApiResponse.error(400, "用户名已存在"));

        // 执行请求并验证
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Mock成功登录
        when(userService.login(any(UserLoginRequest.class), anyString()))
                .thenReturn(ApiResponse.success("登录成功", "mock.jwt.token"));

        // 执行请求并验证
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").value("mock.jwt.token"));
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // Mock登录失败
        when(userService.login(any(UserLoginRequest.class), anyString()))
                .thenReturn(ApiResponse.error(400, "用户名或密码错误"));

        // 执行请求并验证
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetUserInfoSuccess() throws Exception {
        Long userId = 1001L;
        
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // Mock获取用户信息成功
        when(userService.getUserById(eq(userId), eq(1001L)))
                .thenReturn(ApiResponse.success("操作成功", userResponse));

        // 执行请求并验证
        mockMvc.perform(get("/user/{userId}", userId)
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.userId").value(1001L))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testGetUserInfoWithoutToken() throws Exception {
        Long userId = 1001L;

        // 执行请求并验证（没有携带token）
        mockMvc.perform(get("/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void testGetUserInfoWithInvalidToken() throws Exception {
        Long userId = 1001L;
        
        // Mock JWT验证失败
        when(jwtUtil.validateToken("invalid.jwt.token")).thenReturn(false);

        // 执行请求并验证
        mockMvc.perform(get("/user/{userId}", userId)
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void testGetUserListSuccess() throws Exception {
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // 准备分页数据
        List<UserResponse> users = Arrays.asList(userResponse);
        Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        // Mock获取用户列表成功
        when(userService.getUserList(eq(1001L), any()))
                .thenReturn(ApiResponse.success("查询成功", page));

        // 执行请求并验证
        mockMvc.perform(get("/user/users")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", validToken))                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        Long userId = 1001L;
        
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // 准备更新后的用户数据
        UserResponse updatedUser = new UserResponse();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setPhone("13900139000");
        
        // Mock更新用户成功
        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class), eq(1001L), anyString()))
                .thenReturn(ApiResponse.success("更新成功", updatedUser));

        // 执行请求并验证
        mockMvc.perform(put("/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"));
    }

    @Test
    void testResetPasswordSuccess() throws Exception {
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // 准备重置密码请求
        UserController.ResetPasswordRequest resetRequest = new UserController.ResetPasswordRequest();
        resetRequest.setUserId(1002L);
        resetRequest.setNewPassword("newpassword123");
        
        // Mock重置密码成功
        when(userService.resetPassword(eq(1002L), eq("newpassword123"), eq(1001L), anyString()))
                .thenReturn(ApiResponse.success("密码重置成功", "密码重置成功"));

        // 执行请求并验证
        mockMvc.perform(post("/user/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest))
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码重置成功"));
    }

    @Test
    void testGetCurrentUserRoleSuccess() throws Exception {
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // Mock获取角色成功
        when(userService.getUserRole(1001L)).thenReturn("admin");

        // 执行请求并验证
        mockMvc.perform(get("/user/current-role")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色成功"))
                .andExpect(jsonPath("$.data").value("admin"));
    }

    @Test
    void testHealthCheck() throws Exception {
        // 执行请求并验证
        mockMvc.perform(get("/user/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户服务运行正常"));
    }

    @Test
    void testGetUserInfoNoPermission() throws Exception {
        Long userId = 1002L; // 查看其他用户
        
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // Mock无权限查看
        when(userService.getUserById(eq(userId), eq(1001L)))
                .thenReturn(ApiResponse.error(403, "没有权限查看该用户信息"));

        // 执行请求并验证
        mockMvc.perform(get("/user/{userId}", userId)
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("没有权限查看该用户信息"));
    }

    @Test
    void testUpdateUserNoPermission() throws Exception {
        Long userId = 1002L; // 更新其他用户
        
        // Mock JWT验证成功
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1001L);
        
        // Mock无权限更新
        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class), eq(1001L), anyString()))
                .thenReturn(ApiResponse.error(403, "没有权限更新该用户信息"));

        // 执行请求并验证
        mockMvc.perform(put("/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("没有权限更新该用户信息"));
    }
}
