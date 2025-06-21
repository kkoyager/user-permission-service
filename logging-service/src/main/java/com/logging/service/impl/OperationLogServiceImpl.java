package com.logging.service.impl;

import com.logging.entity.OperationLog;
import com.logging.event.OperationLogEvent;
import com.logging.repository.OperationLogRepository;
import com.logging.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Override
    @Transactional
    public void handleLogEvent(OperationLogEvent event) {
        try {
            log.info("处理操作日志事件: userId={}, action={}", event.getUserId(), event.getAction());
            
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(event.getUserId());
            operationLog.setAction(event.getAction());
            operationLog.setIp(event.getIp());
            operationLog.setDetail(event.getDetail());
            
            saveLog(operationLog);
            
            log.info("操作日志保存成功: logId={}", operationLog.getLogId());
        } catch (Exception e) {
            log.error("处理操作日志事件失败: userId={}, action={}, error={}", 
                     event.getUserId(), event.getAction(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OperationLog saveLog(OperationLog log) {
        return operationLogRepository.save(log);
    }

    @Override
    public List<OperationLog> getLogsByUserId(Long userId) {
        return operationLogRepository.findByUserIdOrderByGmtCreateDesc(userId);
    }

    @Override
    public List<OperationLog> getLogsByAction(String action) {
        return operationLogRepository.findByActionOrderByGmtCreateDesc(action);
    }

    @Override
    public Page<OperationLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return operationLogRepository.findByTimeRange(startTime, endTime, pageable);
    }

    @Override
    public Long countUserOperations(Long userId) {
        return operationLogRepository.countByUserId(userId);
    }
}
