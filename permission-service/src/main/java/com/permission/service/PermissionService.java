package com.permission.service;

public interface PermissionService {
    // 绑定默认角色（普通用户）  
    void bindDefaultRole(Long userId);  

    // 查询用户角色码（返回role_code）  
    String getUserRoleCode(Long userId);  

    // 超管调用：升级用户为管理员  
    void upgradeToAdmin(Long userId);  

    // 超管调用：降级用户为普通角色  
    void downgradeToUser(Long userId);
}
