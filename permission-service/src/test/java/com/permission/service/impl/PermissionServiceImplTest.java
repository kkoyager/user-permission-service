package com.permission.service.impl;

import com.permission.entity.Role;
import com.permission.entity.UserRole;
import com.permission.repository.RoleRepository;
import com.permission.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Long testUserId;
    private UserRole testUserRole;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        
        testUserRole = new UserRole();
        testUserRole.setUserId(testUserId);
        testUserRole.setRoleId(2);
          testRole = new Role();
        testRole.setRoleId(2);
        testRole.setRoleCode("USER");
    }    @Test
    void bindDefaultRole_Success() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

        // When
        permissionService.bindDefaultRole(testUserId);

        // Then
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        
        UserRole savedUserRole = userRoleCaptor.getValue();
        System.out.println("绑定默认角色测试 - 保存的用户角色信息:");
        System.out.println("用户ID: " + savedUserRole.getUserId());
        System.out.println("角色ID: " + savedUserRole.getRoleId());
        
        assertEquals(testUserId, savedUserRole.getUserId());
        assertEquals(2, savedUserRole.getRoleId()); // 默认用户角色ID为2
    }    @Test
    void bindDefaultRole_UserAlreadyHasRole() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));

        // When
        permissionService.bindDefaultRole(testUserId);

        // Then
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository, never()).save(any(UserRole.class));
        
        System.out.println("用户已有角色测试 - 当前用户角色信息:");
        System.out.println("用户ID: " + testUserRole.getUserId());
        System.out.println("已存在的角色ID: " + testUserRole.getRoleId());
        System.out.println("操作结果: 跳过绑定，未创建新角色");
    }    @Test
    void getUserRoleCode_Success() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(roleRepository.findById(2)).thenReturn(Optional.of(testRole));

        // When
        String roleCode = permissionService.getUserRoleCode(testUserId);

        // Then
        assertEquals("USER", roleCode);
        verify(userRoleRepository).findByUserId(testUserId);
        verify(roleRepository).findById(2);
        
        System.out.println("获取用户角色代码测试 - 结果:");
        System.out.println("用户ID: " + testUserId);
        System.out.println("用户角色ID: " + testUserRole.getRoleId());
        System.out.println("角色代码: " + roleCode);
    }    @Test
    void getUserRoleCode_UserNotFound() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            permissionService.getUserRoleCode(testUserId);
        });
        
        verify(userRoleRepository).findByUserId(testUserId);
        verify(roleRepository, never()).findById(any());
        
        System.out.println("用户不存在测试 - 异常信息:");
        System.out.println("查询的用户ID: " + testUserId);
        System.out.println("异常类型: " + exception.getClass().getSimpleName());
        System.out.println("异常消息: " + exception.getMessage());
    }    @Test
    void getUserRoleCode_RoleNotFound() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            permissionService.getUserRoleCode(testUserId);
        });
        
        verify(userRoleRepository).findByUserId(testUserId);
        verify(roleRepository).findById(2);
        
        System.out.println("角色不存在测试 - 异常信息:");
        System.out.println("用户ID: " + testUserId);
        System.out.println("查询的角色ID: " + testUserRole.getRoleId());
        System.out.println("异常类型: " + exception.getClass().getSimpleName());
        System.out.println("异常消息: " + exception.getMessage());
    }    @Test
    void upgradeToAdmin_Success() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

        System.out.println("升级管理员测试 - 操作前用户角色信息:");
        System.out.println("用户ID: " + testUserRole.getUserId());
        System.out.println("原角色ID: " + testUserRole.getRoleId());

        // When
        permissionService.upgradeToAdmin(testUserId);

        // Then
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository).save(testUserRole);
        assertEquals(3, testUserRole.getRoleId()); // 管理员角色ID为3
        
        System.out.println("升级管理员测试 - 操作后用户角色信息:");
        System.out.println("用户ID: " + testUserRole.getUserId());
        System.out.println("新角色ID: " + testUserRole.getRoleId());
        System.out.println("操作结果: 成功升级为管理员");
    }    @Test
    void upgradeToAdmin_UserNotFound() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            permissionService.upgradeToAdmin(testUserId);
        });
        
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository, never()).save(any());
        
        System.out.println("升级管理员失败测试 - 异常信息:");
        System.out.println("查询的用户ID: " + testUserId);
        System.out.println("异常类型: " + exception.getClass().getSimpleName());
        System.out.println("异常消息: " + exception.getMessage());
        System.out.println("操作结果: 升级失败，用户不存在");
    }    @Test
    void downgradeToUser_Success() {
        // Given
        testUserRole.setRoleId(3); // 初始为管理员
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

        System.out.println("降级普通用户测试 - 操作前用户角色信息:");
        System.out.println("用户ID: " + testUserRole.getUserId());
        System.out.println("原角色ID: " + testUserRole.getRoleId() + " (管理员)");

        // When
        permissionService.downgradeToUser(testUserId);

        // Then
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository).save(testUserRole);
        assertEquals(2, testUserRole.getRoleId()); // 普通用户角色ID为2
        
        System.out.println("降级普通用户测试 - 操作后用户角色信息:");
        System.out.println("用户ID: " + testUserRole.getUserId());
        System.out.println("新角色ID: " + testUserRole.getRoleId() + " (普通用户)");
        System.out.println("操作结果: 成功降级为普通用户");
    }    @Test
    void downgradeToUser_UserNotFound() {
        // Given
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            permissionService.downgradeToUser(testUserId);
        });
        
        verify(userRoleRepository).findByUserId(testUserId);
        verify(userRoleRepository, never()).save(any());
        
        System.out.println("降级普通用户失败测试 - 异常信息:");
        System.out.println("查询的用户ID: " + testUserId);
        System.out.println("异常类型: " + exception.getClass().getSimpleName());
        System.out.println("异常消息: " + exception.getMessage());
        System.out.println("操作结果: 降级失败，用户不存在");
    }

    /**
     * 综合测试：模拟完整的用户角色生命周期
     */
    @Test
    void userRoleLifecycle_IntegrationTest() {
        System.out.println("\n=== 用户角色生命周期集成测试 ===");
        
        // 1. 绑定默认角色
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);
        
        permissionService.bindDefaultRole(testUserId);
        System.out.println("1. 绑定默认角色完成");
        
        // 2. 查询角色代码
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(roleRepository.findById(2)).thenReturn(Optional.of(testRole));
        
        String roleCode = permissionService.getUserRoleCode(testUserId);
        System.out.println("2. 当前角色代码: " + roleCode);
        
        // 3. 升级为管理员
        permissionService.upgradeToAdmin(testUserId);
        System.out.println("3. 升级为管理员完成，新角色ID: " + testUserRole.getRoleId());
        
        // 4. 降级为普通用户
        permissionService.downgradeToUser(testUserId);
        System.out.println("4. 降级为普通用户完成，新角色ID: " + testUserRole.getRoleId());
        
        System.out.println("=== 用户角色生命周期测试完成 ===\n");
        
        // 验证最终状态
        assertEquals(2, testUserRole.getRoleId());
        verify(userRoleRepository, times(3)).save(any(UserRole.class));
    }
}
