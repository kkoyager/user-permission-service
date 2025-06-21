package com.logging.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 记录用户的各种操作行为
 */
@Entity
@Table(name = "operation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "ip", length = 15)
    private String ip;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;

    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
    }
}
