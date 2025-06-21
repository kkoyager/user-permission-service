package com.logging.service;

import com.logging.entity.OperationLog;
import com.logging.event.OperationLogEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 处理操作日志事件
     */
    void handleLogEvent(OperationLogEvent event);

    /**
     * 保存操作日志
     */
    OperationLog saveLog(OperationLog log);

    /**
     * 根据用户ID查询操作日志
     */
    List<OperationLog> getLogsByUserId(Long userId);

    /**
     * 根据操作类型查询日志
     */
    List<OperationLog> getLogsByAction(String action);

    /**
     * 分页查询指定时间范围的日志
     */
    Page<OperationLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 统计用户操作次数
     */
    Long countUserOperations(Long userId);
}
