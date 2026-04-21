package com.mxbc.seckill.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "system_metrics")
public class SystemMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "order_count")
    private Integer orderCount;
    
    @Column(name = "revenue")
    private Double revenue;
    
    @Column(name = "user_count")
    private Integer userCount;
    
    @Column(name = "product_count")
    private Integer productCount;
    
    @Column(name = "activity_count")
    private Integer activityCount;
    
    @Column(name = "active_activity_count")
    private Integer activeActivityCount;
    
    @Column(name = "heap_used_mb")
    private Double heapUsedMb;
    
    @Column(name = "heap_max_mb")
    private Double heapMaxMb;
    
    @Column(name = "heap_usage_percent")
    private Double heapUsagePercent;
    
    @Column(name = "redis_connections")
    private Integer redisConnections;
    
    @Column(name = "redis_healthy")
    private Boolean redisHealthy;
    
    @Column(name = "database_healthy")
    private Boolean databaseHealthy;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }
    
    public Double getRevenue() {
        return revenue;
    }
    
    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
    
    public Integer getUserCount() {
        return userCount;
    }
    
    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }
    
    public Integer getProductCount() {
        return productCount;
    }
    
    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }
    
    public Integer getActivityCount() {
        return activityCount;
    }
    
    public void setActivityCount(Integer activityCount) {
        this.activityCount = activityCount;
    }
    
    public Integer getActiveActivityCount() {
        return activeActivityCount;
    }
    
    public void setActiveActivityCount(Integer activeActivityCount) {
        this.activeActivityCount = activeActivityCount;
    }
    
    public Double getHeapUsedMb() {
        return heapUsedMb;
    }
    
    public void setHeapUsedMb(Double heapUsedMb) {
        this.heapUsedMb = heapUsedMb;
    }
    
    public Double getHeapMaxMb() {
        return heapMaxMb;
    }
    
    public void setHeapMaxMb(Double heapMaxMb) {
        this.heapMaxMb = heapMaxMb;
    }
    
    public Double getHeapUsagePercent() {
        return heapUsagePercent;
    }
    
    public void setHeapUsagePercent(Double heapUsagePercent) {
        this.heapUsagePercent = heapUsagePercent;
    }
    
    public Integer getRedisConnections() {
        return redisConnections;
    }
    
    public void setRedisConnections(Integer redisConnections) {
        this.redisConnections = redisConnections;
    }
    
    public Boolean getRedisHealthy() {
        return redisHealthy;
    }
    
    public void setRedisHealthy(Boolean redisHealthy) {
        this.redisHealthy = redisHealthy;
    }
    
    public Boolean getDatabaseHealthy() {
        return databaseHealthy;
    }
    
    public void setDatabaseHealthy(Boolean databaseHealthy) {
        this.databaseHealthy = databaseHealthy;
    }
}