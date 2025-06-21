package com.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 * 
 * @author developer
 * @since 2024-06-21
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("====================================");
        System.out.println("用户服务启动成功！");
        System.out.println("====================================");
    }
}
