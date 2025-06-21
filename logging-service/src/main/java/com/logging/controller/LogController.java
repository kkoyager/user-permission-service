package com.logging.controller;

import com.logging.entity.OperationLog;
import com.logging.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志查询接口
 * 提供日志查询相关功能
 */
@RestController
@RequestMapping("/logs")
@Slf4j
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 根据用户ID查询操作日志
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserLogs(@PathVariable Long userId) {
        try {
            List<OperationLog> logs = operationLogService.getLogsByUserId(userId);
            Long count = operationLogService.countUserOperations(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("logs", logs);
            result.put("total", count);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询用户日志失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据操作类型查询日志
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<OperationLog>> getLogsByAction(@PathVariable String action) {
        try {
            List<OperationLog> logs = operationLogService.getLogsByAction(action);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("根据操作类型查询日志失败: action={}, error={}", action, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 分页查询指定时间范围的日志
     */
    @GetMapping("/range")
    public ResponseEntity<Page<OperationLog>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OperationLog> logs = operationLogService.getLogsByTimeRange(startTime, endTime, pageable);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("查询时间范围日志失败: startTime={}, endTime={}, error={}", 
                     startTime, endTime, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Logging Service is running");
    }
}
