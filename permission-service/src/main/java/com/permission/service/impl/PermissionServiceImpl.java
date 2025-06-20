package com.permission.service.impl;

import com.permission.entity.Role;
import com.permission.entity.UserRole;
import com.permission.repository.RoleRepository;
import com.permission.repository.UserRoleRepository;
import com.permission.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    
    // 角色ID常量
    private static final int ROLE_USER_ID = 2; // 普通用户
    private static final int ROLE_ADMIN_ID = 3; // 管理员
    
    @Autowired
    public PermissionServiceImpl(UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }
    
    @Override
    @Transactional
    public void bindDefaultRole(Long userId) {
        log.info("Binding default role for user: {}", userId);
        
        // 检查是否已经有角色绑定
        if (userRoleRepository.findByUserId(userId).isPresent()) {
            log.warn("User {} already has a role assigned", userId);
            return;
        }
        
        // 绑定普通用户角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(ROLE_USER_ID); // 普通用户角色ID
        
        userRoleRepository.save(userRole);
        log.info("Default role bound for user: {}", userId);
    }

    @Override
    public String getUserRoleCode(Long userId) {
        log.info("Getting role code for user: {}", userId);
        
        // 查询用户角色关系
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User role not found for user: " + userId));
        
        // 查询角色代码
        Role role = roleRepository.findById(userRole.getRoleId())
            .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + userRole.getRoleId()));
        
        log.info("User {} has role: {}", userId, role.getRoleCode());
        return role.getRoleCode();
    }

    @Override
    @Transactional
    public void upgradeToAdmin(Long userId) {
        log.info("Upgrading user {} to admin role", userId);
        
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User role not found for user: " + userId));
        
        userRole.setRoleId(ROLE_ADMIN_ID); // 管理员角色ID
        userRoleRepository.save(userRole);
        
        log.info("User {} upgraded to admin role", userId);
    }

    @Override
    @Transactional
    public void downgradeToUser(Long userId) {
        log.info("Downgrading user {} to normal user role", userId);
        
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User role not found for user: " + userId));
        
        userRole.setRoleId(ROLE_USER_ID); // 普通用户角色ID
        userRoleRepository.save(userRole);
        
        log.info("User {} downgraded to normal user role", userId);
    }
}
