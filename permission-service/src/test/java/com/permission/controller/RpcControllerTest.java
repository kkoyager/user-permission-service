package com.permission.controller;

import com.permission.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RpcController 控制器测试
 */
@WebMvcTest(RpcController.class)
class RpcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @Test
    void bindDefaultRole_Success() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(permissionService).bindDefaultRole(userId);

        // When & Then
        mockMvc.perform(post("/rpc/user/{userId}/role/default", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(permissionService).bindDefaultRole(userId);
    }

    @Test
    void getUserRoleCode_Success() throws Exception {
        // Given
        Long userId = 1L;
        String expectedRoleCode = "USER";
        when(permissionService.getUserRoleCode(userId)).thenReturn(expectedRoleCode);

        // When & Then
        mockMvc.perform(get("/rpc/user/{userId}/role", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedRoleCode));

        verify(permissionService).getUserRoleCode(userId);
    }

    @Test
    void getUserRoleCode_UserNotFound() throws Exception {
        // Given
        Long userId = 1L;
        when(permissionService.getUserRoleCode(userId))
                .thenThrow(new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"));

        // When & Then
        mockMvc.perform(get("/rpc/user/{userId}/role", userId))
                .andExpect(status().isInternalServerError());

        verify(permissionService).getUserRoleCode(userId);
    }    @Test
    void upgradeToAdmin_Success() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(permissionService).upgradeToAdmin(userId);

        // When & Then
        mockMvc.perform(put("/rpc/user/{userId}/role/admin", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(permissionService).upgradeToAdmin(userId);
    }

    @Test
    void upgradeToAdmin_UserNotFound() throws Exception {
        // Given
        Long userId = 1L;
        doThrow(new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"))
                .when(permissionService).upgradeToAdmin(userId);

        // When & Then
        mockMvc.perform(put("/rpc/user/{userId}/role/admin", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(permissionService).upgradeToAdmin(userId);
    }

    @Test
    void downgradeToUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(permissionService).downgradeToUser(userId);

        // When & Then
        mockMvc.perform(put("/rpc/user/{userId}/role/user", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(permissionService).downgradeToUser(userId);
    }    @Test
    void downgradeToUser_UserNotFound() throws Exception {
        // Given
        Long userId = 1L;
        doThrow(new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"))
                .when(permissionService).downgradeToUser(userId);

        // When & Then
        mockMvc.perform(put("/rpc/user/{userId}/role/user", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(permissionService).downgradeToUser(userId);
    }
}
