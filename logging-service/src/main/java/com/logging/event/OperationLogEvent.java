package com.logging.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 操作日志事件
 * 用于MQ消息传输的数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 用户IP地址
     */
    private String ip;

    /**
     * 操作详情
     */
    private String detail;

    /**
     * 创建带基本信息的日志事件
     */
    public static OperationLogEvent of(Long userId, String action, String ip) {
        return new OperationLogEvent(userId, action, ip, null);
    }

    /**
     * 创建带详细信息的日志事件
     */
    public static OperationLogEvent of(Long userId, String action, String ip, String detail) {
        return new OperationLogEvent(userId, action, ip, detail);
    }
}
