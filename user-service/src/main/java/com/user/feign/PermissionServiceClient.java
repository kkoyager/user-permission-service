package com.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 权限服务RPC客户端
 * 通过Feign调用permission-service提供的接口
 * 
 * @author developer
 * @since 2024-06-21
 */
@FeignClient(name = "permission-service", fallback = PermissionServiceFallback.class)
public interface PermissionServiceClient {

    /**
     * 为新注册用户绑定默认角色（普通用户）
     * 在用户注册时调用
     * 
     * @param userId 用户ID
     * @return 绑定结果
     */
    @PostMapping("/permission/bind-default-role")
    String bindDefaultRole(@RequestParam("userId") Long userId);

    /**
     * 查询用户的角色代码
     * 用于权限校验，返回角色码如：user/admin/super_admin
     * 
     * @param userId 用户ID
     * @return 角色代码
     */
    @GetMapping("/permission/user-role/{userId}")
    String getUserRoleCode(@PathVariable("userId") Long userId);

    /**
     * 将用户升级为管理员
     * 只有超管可以调用
     * 
     * @param userId 用户ID
     * @return 升级结果
     */
    @PostMapping("/permission/upgrade-to-admin")
    String upgradeToAdmin(@RequestParam("userId") Long userId);

    /**
     * 将用户降级为普通用户
     * 只有超管可以调用
     * 
     * @param userId 用户ID  
     * @return 降级结果
     */
    @PostMapping("/permission/downgrade-to-user")
    String downgradeToUser(@RequestParam("userId") Long userId);
}
