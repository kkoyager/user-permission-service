package com.user.dto;

/**
 * 用户更新请求DTO
 * 用于更新用户信息，不包含敏感字段
 * 
 * @author developer
 * @since 2024-06-21
 */
public class UserUpdateRequest {

    private String email;
    private String phone;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    // 检查是否有需要更新的字段
    public boolean hasUpdateFields() {
        return (email != null && !email.trim().isEmpty()) 
            || (phone != null && !phone.trim().isEmpty());
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

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
