package com.user.exception;

import com.user.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 统一处理应用中的异常，返回标准的API响应格式
 * 
 * @author developer
 * @since 2024-06-21
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: uri={}, message={}", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("参数异常: uri={}, message={}", request.getRequestURI(), e.getMessage());
        return ApiResponse.badRequest("参数错误: " + e.getMessage());
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.warn("接口不存在: uri={}", request.getRequestURI());
        return ApiResponse.error(404, "接口不存在: " + request.getRequestURI());
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleDataAccessException(org.springframework.dao.DataAccessException e, HttpServletRequest request) {
        logger.error("数据库异常: uri={}, error={}", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error("数据库操作失败，请稍后重试");
    }

    /**
     * 处理RPC调用异常
     */
    @ExceptionHandler(feign.FeignException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<String> handleFeignException(feign.FeignException e, HttpServletRequest request) {
        logger.error("RPC调用异常: uri={}, status={}, message={}", 
                request.getRequestURI(), e.status(), e.getMessage());
        return ApiResponse.error(503, "服务暂时不可用，请稍后重试");
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: uri={}, error={}", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error("系统异常，请联系管理员");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: uri={}, error={}", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error("系统运行异常: " + e.getMessage());
    }
}
