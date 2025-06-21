package com.user.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 权限服务Feign调用失败降级处理
 * 当权限服务不可用时提供默认实现
 * 
 * @author developer
 * @since 2024-06-21
 */
@Component
public class PermissionServiceFallback implements PermissionServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceFallback.class);

    @Override
    public String bindDefaultRole(Long userId) {
        logger.error("权限服务调用失败，无法为用户{}绑定默认角色", userId);
        // 返回失败标识，业务层需要处理这种情况
        return "FALLBACK_ERROR";
    }

    @Override
    public String getUserRoleCode(Long userId) {
        logger.error("权限服务调用失败，无法获取用户{}的角色信息", userId);
        // 降级策略：返回最低权限角色
        return "user";
    }

    @Override
    public String upgradeToAdmin(Long userId) {
        logger.error("权限服务调用失败，无法升级用户{}为管理员", userId);
        return "FALLBACK_ERROR";
    }

    @Override
    public String downgradeToUser(Long userId) {
        logger.error("权限服务调用失败，无法降级用户{}", userId);
        return "FALLBACK_ERROR";
    }
}
