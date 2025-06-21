package com.permission.controller;

import com.permission.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 权限服务RPC控制器
 * 提供用户角色管理的远程调用接口
 */
@RestController
@RequestMapping("/rpc")
@Slf4j
public class RpcController {

    private final PermissionService permissionService;

    @Autowired
    public RpcController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    /**
     * 为用户绑定默认角色（普通用户）
     */
    @PostMapping("/user/{userId}/role/default")
    public ResponseEntity<Void> bindDefaultRole(@PathVariable Long userId) {
        log.info("接收到RPC调用：为用户 {} 绑定默认角色", userId);
        permissionService.bindDefaultRole(userId);
        log.info("成功为用户 {} 绑定默认角色", userId);
        return ResponseEntity.ok().build();
    }    /**
     * 查询用户角色代码
     */
    @GetMapping("/user/{userId}/role")
    public ResponseEntity<String> getUserRoleCode(@PathVariable Long userId) {
        log.info("接收到RPC调用：查询用户 {} 的角色代码", userId);
        String roleCode = permissionService.getUserRoleCode(userId);
        log.info("用户 {} 的角色代码为：{}", userId, roleCode);
        return ResponseEntity.ok(roleCode);
    }    /**
     * 将用户升级为管理员角色
     */
    @PutMapping("/user/{userId}/role/admin")
    public ResponseEntity<Void> upgradeToAdmin(@PathVariable Long userId) {
        log.info("接收到RPC调用：将用户 {} 升级为管理员", userId);
        permissionService.upgradeToAdmin(userId);
        log.info("成功将用户 {} 升级为管理员", userId);
        return ResponseEntity.ok().build();
    }    /**
     * 将用户降级为普通用户角色
     */
    @PutMapping("/user/{userId}/role/user")
    public ResponseEntity<Void> downgradeToUser(@PathVariable Long userId) {
        log.info("接收到RPC调用：将用户 {} 降级为普通用户", userId);
        permissionService.downgradeToUser(userId);
        log.info("成功将用户 {} 降级为普通用户", userId);
        return ResponseEntity.ok().build();
    }
}
