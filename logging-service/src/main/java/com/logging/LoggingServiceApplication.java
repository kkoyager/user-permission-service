package com.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 日志服务启动类
 * 负责异步消费操作日志消息并持久化存储
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LoggingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApplication.class, args);
        System.out.println("=== 日志服务启动完成 ===");
    }
}
