package com.logging.repository;

import com.logging.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问层
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 根据用户ID查询操作日志
     */
    List<OperationLog> findByUserIdOrderByGmtCreateDesc(Long userId);

    /**
     * 根据操作类型查询日志
     */
    List<OperationLog> findByActionOrderByGmtCreateDesc(String action);

    /**
     * 查询指定时间范围的日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.gmtCreate BETWEEN :startTime AND :endTime ORDER BY ol.gmtCreate DESC")
    Page<OperationLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime, 
                                      Pageable pageable);

    /**
     * 统计用户操作次数
     */
    Long countByUserId(Long userId);
}
