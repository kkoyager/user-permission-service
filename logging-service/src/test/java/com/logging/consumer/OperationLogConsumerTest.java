package com.logging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logging.event.OperationLogEvent;
import com.logging.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationLogConsumerTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OperationLogConsumer operationLogConsumer;

    private OperationLogEvent sampleEvent;    @BeforeEach
    void setUp() {
        sampleEvent = new OperationLogEvent();
        sampleEvent.setUserId(1L);
        sampleEvent.setAction("创建用户");
        sampleEvent.setIp("192.168.1.1");
        sampleEvent.setDetail("创建用户操作");
    }

    @Test
    void testOnMessage_Success() throws Exception {
        // Given
        String message = "{\"userId\":1,\"action\":\"创建用户\"}";
        when(objectMapper.readValue(message, OperationLogEvent.class)).thenReturn(sampleEvent);

        // When
        operationLogConsumer.onMessage(message);

        // Then
        verify(operationLogService, times(1)).handleLogEvent(any(OperationLogEvent.class));
    }

    @Test
    void testOnMessage_InvalidJson() throws Exception {
        // Given
        String invalidMessage = "invalid json";
        when(objectMapper.readValue(invalidMessage, OperationLogEvent.class))
            .thenThrow(new RuntimeException("JSON解析失败"));

        // When & Then
        try {
            operationLogConsumer.onMessage(invalidMessage);
        } catch (RuntimeException e) {
            // 预期会抛出异常
        }
        
        // 验证service没有被调用
        verify(operationLogService, never()).handleLogEvent(any(OperationLogEvent.class));
    }
}
