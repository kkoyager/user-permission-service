package com.user.util;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息发送工具类
 * 用于发送操作日志到RocketMQ
 * 
 * @author developer
 * @since 2024-06-21
 */
@Component
public class MessageUtil {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 日志主题
    private static final String LOG_TOPIC = "operation-log-topic";

    /**
     * 发送用户操作日志消息
     * 
     * @param userId 用户ID
     * @param action 操作类型
     * @param ip 用户IP地址
     * @param detail 操作详情
     */
    public void sendOperationLog(Long userId, String action, String ip, String detail) {
        try {
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("userId", userId);
            logMessage.put("action", action);
            logMessage.put("ip", ip);
            logMessage.put("detail", detail);
            logMessage.put("timestamp", LocalDateTime.now().toString());

            // 异步发送消息到RocketMQ
            rocketMQTemplate.asyncSend(LOG_TOPIC, logMessage, new MessageSendCallback(action, userId));
            
            logger.info("操作日志消息发送成功: userId={}, action={}", userId, action);
        } catch (Exception e) {
            logger.error("发送操作日志消息失败: userId={}, action={}, error={}", userId, action, e.getMessage());
        }
    }

    /**
     * 发送用户注册日志
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param ip IP地址
     */
    public void sendRegisterLog(Long userId, String username, String ip) {
        String detail = String.format("用户注册: username=%s", username);
        sendOperationLog(userId, "USER_REGISTER", ip, detail);
    }

    /**
     * 发送用户登录日志
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param ip IP地址
     */
    public void sendLoginLog(Long userId, String username, String ip) {
        String detail = String.format("用户登录: username=%s", username);
        sendOperationLog(userId, "USER_LOGIN", ip, detail);
    }

    /**
     * 发送用户信息更新日志
     * 
     * @param userId 用户ID
     * @param updateFields 更新的字段信息
     * @param ip IP地址
     */
    public void sendUpdateLog(Long userId, String updateFields, String ip) {
        String detail = String.format("用户信息更新: %s", updateFields);
        sendOperationLog(userId, "USER_UPDATE", ip, detail);
    }

    /**
     * 发送密码重置日志
     * 
     * @param userId 用户ID
     * @param ip IP地址
     */
    public void sendPasswordResetLog(Long userId, String ip) {
        String detail = "用户密码重置";
        sendOperationLog(userId, "PASSWORD_RESET", ip, detail);
    }

    /**
     * 消息发送回调类
     * 用于处理异步发送的结果
     */
    private static class MessageSendCallback implements org.apache.rocketmq.client.producer.SendCallback {
        
        private final String action;
        private final Long userId;

        public MessageSendCallback(String action, Long userId) {
            this.action = action;
            this.userId = userId;
        }

        @Override
        public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
            logger.debug("消息发送成功: action={}, userId={}, msgId={}", 
                    action, userId, sendResult.getMsgId());
        }

        @Override
        public void onException(Throwable e) {
            logger.error("消息发送失败: action={}, userId={}, error={}", 
                    action, userId, e.getMessage());
        }
    }
}
