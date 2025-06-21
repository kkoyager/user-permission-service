package com.user.dto;

/**
 * 用户注册请求DTO
 * 
 * @author developer
 * @since 2024-06-21
 */
public class UserRegisterRequest {

    private String username;
    private String password;
    private String email;
    private String phone;

    public UserRegisterRequest() {
    }

    public UserRegisterRequest(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    // 基本校验方法
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() 
            && password != null && !password.trim().isEmpty();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        return "UserRegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
