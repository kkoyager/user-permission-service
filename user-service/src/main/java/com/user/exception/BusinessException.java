package com.user.exception;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 * 
 * @author developer
 * @since 2024-06-21
 */
public class BusinessException extends RuntimeException {

    private int code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // 静态工厂方法，便于创建常见的业务异常
    public static BusinessException userNotFound() {
        return new BusinessException(404, "用户不存在");
    }

    public static BusinessException userAlreadyExists() {
        return new BusinessException("用户已存在");
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(401, "用户名或密码错误");
    }

    public static BusinessException accessDenied() {
        return new BusinessException(403, "权限不足");
    }

    public static BusinessException serviceUnavailable() {
        return new BusinessException(503, "服务暂时不可用");
    }
}
