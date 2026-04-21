package com.mxbc.seckill.entity.repository;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 根据订单号查询订单
    @Query("SELECT o FROM Order o WHERE o.order_id = :orderId")
    Order findByOrderId(@Param("orderId") String orderId);
    
    // 根据用户ID查询订单列表
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);
    
    // 根据用户ID查询订单列表，按创建时间倒序
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // 根据用户ID和状态查询订单
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    // 根据状态查询订单
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);
    
    // 查询指定时间之前的待支付订单
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :beforeTime")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") OrderStatus status, @Param("beforeTime") LocalDateTime beforeTime);
    
    // 统计用户的订单数量
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // 统计指定状态的订单数量
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);
    
    // 查询指定时间范围内的订单
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startTime AND :endTime ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 根据订单号和用户ID查询订单
    @Query("SELECT o FROM Order o WHERE o.order_id = :orderId AND o.user.id = :userId")
    Order findByOrderIdAndUserId(@Param("orderId") String orderId, @Param("userId") Long userId);
    
    // 查询用户的待支付订单数量
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    // 查询指定时间范围内的订单数量
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 查询指定时间范围内的订单总金额
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startTime AND :endTime AND o.status = :status")
    BigDecimal sumAmountByCreatedAtBetweenAndStatus(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("status") OrderStatus status);
    
    // 根据状态列表查询订单
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses);
}
