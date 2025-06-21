package com.logging.service.impl;

import com.logging.entity.OperationLog;
import com.logging.event.OperationLogEvent;
import com.logging.repository.OperationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogRepository operationLogRepository;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    private OperationLog sampleLog;
    private OperationLogEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleLog = new OperationLog();
        sampleLog.setLogId(1L);
        sampleLog.setUserId(1001L);
        sampleLog.setAction("CREATE_USER");
        sampleLog.setDetail("创建用户操作");
        sampleLog.setIp("192.168.1.100");
        sampleLog.setGmtCreate(LocalDateTime.now());

        sampleEvent = new OperationLogEvent();
        sampleEvent.setUserId(1001L);
        sampleEvent.setAction("CREATE_USER");
        sampleEvent.setIp("192.168.1.100");
        sampleEvent.setDetail("创建用户操作");
    }

    @Test
    void testHandleLogEvent() {
        // Given
        when(operationLogRepository.save(any(OperationLog.class))).thenReturn(sampleLog);

        // When
        operationLogService.handleLogEvent(sampleEvent);

        // Then
        verify(operationLogRepository, times(1)).save(any(OperationLog.class));
    }

    @Test
    void testSaveLog() {
        // Given
        when(operationLogRepository.save(any(OperationLog.class))).thenReturn(sampleLog);

        // When
        OperationLog result = operationLogService.saveLog(sampleLog);

        // Then
        assertNotNull(result);
        assertEquals(sampleLog.getUserId(), result.getUserId());
        assertEquals(sampleLog.getAction(), result.getAction());
        verify(operationLogRepository, times(1)).save(any(OperationLog.class));
    }

    @Test
    void testGetLogsByUserId() {
        // Given
        List<OperationLog> logs = Arrays.asList(sampleLog);
        when(operationLogRepository.findByUserIdOrderByGmtCreateDesc(1001L)).thenReturn(logs);

        // When
        List<OperationLog> result = operationLogService.getLogsByUserId(1001L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleLog.getUserId(), result.get(0).getUserId());
        verify(operationLogRepository, times(1)).findByUserIdOrderByGmtCreateDesc(1001L);
    }

    @Test
    void testGetLogsByAction() {
        // Given
        List<OperationLog> logs = Arrays.asList(sampleLog);
        when(operationLogRepository.findByActionOrderByGmtCreateDesc("CREATE_USER")).thenReturn(logs);

        // When
        List<OperationLog> result = operationLogService.getLogsByAction("CREATE_USER");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleLog.getAction(), result.get(0).getAction());
        verify(operationLogRepository, times(1)).findByActionOrderByGmtCreateDesc("CREATE_USER");
    }

    @Test
    void testGetLogsByTimeRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);
        List<OperationLog> logs = Arrays.asList(sampleLog);
        Page<OperationLog> page = new PageImpl<>(logs, pageable, 1);
        when(operationLogRepository.findByTimeRange(start, end, pageable)).thenReturn(page);

        // When
        Page<OperationLog> result = operationLogService.getLogsByTimeRange(start, end, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(operationLogRepository, times(1)).findByTimeRange(start, end, pageable);
    }

    @Test
    void testCountUserOperations() {
        // Given
        when(operationLogRepository.countByUserId(1001L)).thenReturn(5L);

        // When
        Long result = operationLogService.countUserOperations(1001L);

        // Then
        assertNotNull(result);
        assertEquals(5L, result);
        verify(operationLogRepository, times(1)).countByUserId(1001L);
    }
}
