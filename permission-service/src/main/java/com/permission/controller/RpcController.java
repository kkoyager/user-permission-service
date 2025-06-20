package com.permission.controller;

import com.permission.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rpc")
@Slf4j
public class RpcController {

    private final PermissionService permissionService;

    @Autowired
    public RpcController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/user/{userId}/role/default")
    public ResponseEntity<Void> bindDefaultRole(@PathVariable Long userId) {
        log.info("RPC call to bind default role for user: {}", userId);
        permissionService.bindDefaultRole(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/role")
    public ResponseEntity<String> getUserRoleCode(@PathVariable Long userId) {
        log.info("RPC call to get role code for user: {}", userId);
        String roleCode = permissionService.getUserRoleCode(userId);
        return ResponseEntity.ok(roleCode);
    }

    @PutMapping("/user/{userId}/role/admin")
    public ResponseEntity<Void> upgradeToAdmin(@PathVariable Long userId) {
        log.info("RPC call to upgrade user {} to admin role", userId);
        permissionService.upgradeToAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/role/user")
    public ResponseEntity<Void> downgradeToUser(@PathVariable Long userId) {
        log.info("RPC call to downgrade user {} to normal user role", userId);
        permissionService.downgradeToUser(userId);
        return ResponseEntity.ok().build();
    }
}
