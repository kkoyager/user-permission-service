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

/**
 * 权限服务实现类
 * 负责用户角色的绑定、查询和管理
 */
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
        log.info("开始为用户 {} 绑定默认角色", userId);
        
        // 检查是否已经有角色绑定
        if (userRoleRepository.findByUserId(userId).isPresent()) {
            log.warn("用户 {} 已经分配了角色，跳过绑定", userId);
            return;
        }
        
        // 绑定普通用户角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(ROLE_USER_ID); // 普通用户角色ID
        
        userRoleRepository.save(userRole);
        log.info("成功为用户 {} 绑定默认角色（普通用户）", userId);
    }    @Override
    public String getUserRoleCode(Long userId) {
        log.info("开始查询用户 {} 的角色代码", userId);
        
        // 查询用户角色关系
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"));
        
        // 查询角色代码
        Role role = roleRepository.findById(userRole.getRoleId())
            .orElseThrow(() -> new EntityNotFoundException("未找到角色ID为 " + userRole.getRoleId() + " 的角色"));
        
        log.info("用户 {} 的角色为：{}", userId, role.getRoleCode());
        return role.getRoleCode();
    }    @Override
    @Transactional
    public void upgradeToAdmin(Long userId) {
        log.info("开始将用户 {} 升级为管理员角色", userId);
        
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"));
        
        userRole.setRoleId(ROLE_ADMIN_ID); // 管理员角色ID
        userRoleRepository.save(userRole);
        
        log.info("成功将用户 {} 升级为管理员角色", userId);
    }    @Override
    @Transactional
    public void downgradeToUser(Long userId) {
        log.info("开始将用户 {} 降级为普通用户角色", userId);
        
        UserRole userRole = userRoleRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("未找到用户 " + userId + " 的角色信息"));
        
        userRole.setRoleId(ROLE_USER_ID); // 普通用户角色ID
        userRoleRepository.save(userRole);
        
        log.info("成功将用户 {} 降级为普通用户角色", userId);
    }
}
