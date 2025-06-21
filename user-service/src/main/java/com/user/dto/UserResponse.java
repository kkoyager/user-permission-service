package com.user.dto;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 返回给前端的用户信息，不包含密码等敏感信息
 * 
 * @author developer
 * @since 2024-06-21
 */
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public UserResponse() {
    }

    public UserResponse(Long userId, String username, String email, String phone, 
                       LocalDateTime gmtCreate, LocalDateTime gmtModified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }
}
