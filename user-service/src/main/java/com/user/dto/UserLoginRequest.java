package com.user.dto;

/**
 * 用户登录请求DTO
 * 
 * @author developer
 * @since 2024-06-21  
 */
public class UserLoginRequest {

    private String username;
    private String password;

    public UserLoginRequest() {
    }

    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 校验登录参数
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

    @Override
    public String toString() {
        return "UserLoginRequest{" +
                "username='" + username + '\'' +
                '}';
    }
}
