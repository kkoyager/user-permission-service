package com.logging.controller;

import com.logging.entity.OperationLog;
import com.logging.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationLogService operationLogService;

    private OperationLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new OperationLog();
        sampleLog.setLogId(1L);
        sampleLog.setUserId(1001L);
        sampleLog.setAction("CREATE_USER");
        sampleLog.setDetail("创建用户操作");
        sampleLog.setIp("192.168.1.100");
        sampleLog.setGmtCreate(LocalDateTime.now());
    }

    @Test
    void testGetUserLogs() throws Exception {
        // Given
        List<OperationLog> logs = Arrays.asList(sampleLog);
        when(operationLogService.getLogsByUserId(1001L)).thenReturn(logs);
        when(operationLogService.countUserOperations(1001L)).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/logs/user/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs[0].userId").value(1001))
                .andExpect(jsonPath("$.logs[0].action").value("CREATE_USER"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void testGetLogsByAction() throws Exception {
        // Given
        List<OperationLog> logs = Arrays.asList(sampleLog);
        when(operationLogService.getLogsByAction("CREATE_USER")).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/logs/action/CREATE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].action").value("CREATE_USER"));
    }

    @Test
    void testGetLogsByTimeRange() throws Exception {
        // Given
        List<OperationLog> logs = Arrays.asList(sampleLog);
        Page<OperationLog> page = new PageImpl<>(logs, PageRequest.of(0, 20), 1);
        when(operationLogService.getLogsByTimeRange(any(), any(), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/logs/range")
                .param("startTime", "2025-06-20T00:00:00")
                .param("endTime", "2025-06-21T23:59:59")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].action").value("CREATE_USER"));
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/logs/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logging Service is running"));
    }
}
