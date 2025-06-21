package com.user.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 * 提供密码加密和验证功能
 * 
 * @author developer  
 * @since 2024-06-21
 */
@Component
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * 加密密码
     * 使用SHA-256 + 盐值的方式加密
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码（包含盐值）
     */
    public String encryptPassword(String rawPassword) {
        try {
            // 生成随机盐值
            byte[] salt = generateSalt();
            
            // 使用SHA-256加密
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(rawPassword.getBytes());
            
            // 将盐值和哈希值组合，用Base64编码
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 已加密的密码
     * @return true表示密码正确，false表示密码错误
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        try {
            // 解码Base64获取盐值和哈希值
            byte[] combined = Base64.getDecoder().decode(encodedPassword);
            
            // 提取盐值
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            
            // 提取原哈希值
            byte[] originalHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, originalHash, 0, originalHash.length);
            
            // 使用相同的盐值对输入密码进行哈希
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] inputHash = md.digest(rawPassword.getBytes());
            
            // 比较两个哈希值
            return MessageDigest.isEqual(originalHash, inputHash);
        } catch (Exception e) {
            // 密码验证出错，为安全考虑返回false
            return false;
        }
    }

    /**
     * 生成随机盐值
     * 
     * @return 盐值字节数组
     */
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * 检查密码强度
     * 
     * @param password 密码
     * @return true表示密码强度符合要求
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // 简单的密码强度检查：至少6位字符
        // 实际项目中可以根据需求增加更复杂的规则
        return password.length() >= 6 && password.length() <= 50;
    }
}
