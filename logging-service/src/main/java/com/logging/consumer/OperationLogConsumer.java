package com.logging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logging.event.OperationLogEvent;
import com.logging.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 操作日志消息消费者
 * 监听MQ中的操作日志消息并进行处理
 */
@Component
@Slf4j
@RocketMQMessageListener(
    topic = "operation-log-topic",
    consumerGroup = "logging-service-group"
)
public class OperationLogConsumer implements RocketMQListener<String> {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        try {
            log.info("收到操作日志消息: {}", message);
            
            // 解析消息
            OperationLogEvent event = objectMapper.readValue(message, OperationLogEvent.class);
            
            // 处理日志事件
            operationLogService.handleLogEvent(event);
            
            log.info("操作日志消息处理完成");
            
        } catch (Exception e) {
            log.error("处理操作日志消息失败: message={}, error={}", message, e.getMessage(), e);
            // 这里可以考虑重试机制或者发送到死信队列
            throw new RuntimeException("消息处理失败", e);
        }
    }
}
