package com.user.repository;

import com.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 基于ShardingSphere分库分表的JPA Repository
 * 
 * @author developer
 * @since 2024-06-21
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * ShardingSphere会自动路由到正确的分片
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 分页查询所有用户
     * 用于管理员和超管查看用户列表
     */
    @Override
    Page<User> findAll(Pageable pageable);

    /**
     * 自定义查询：根据用户名模糊搜索
     * 注意：跨分片的模糊查询可能影响性能，生产环境需要优化
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    Page<User> findByUsernameContaining(@Param("username") String username, Pageable pageable);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
}
