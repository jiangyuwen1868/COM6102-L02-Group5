package com.mxbc.seckill.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "stock_logs")
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "before_stock")
    private Integer beforeStock;
    
    @Column(name = "after_stock")
    private Integer afterStock;
    
    @Column(name = "change_amount")
    private Integer changeAmount;
    
    @Enumerated(EnumType.STRING)
    private StockOperationType operationType;
    
    @Column(name = "operation_desc")
    private String operationDesc;
    
    @Column(name = "redis_stock")
    private Integer redisStock;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum StockOperationType {
        INIT("初始化"),
        SYNC("同步"),
        PRELOAD("预热"),
        SECKILL("秒杀"),
        ROLLBACK("回滚"),
        MANUAL("手动调整");
        
        private final String description;
        
        StockOperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Integer getBeforeStock() {
        return beforeStock;
    }
    
    public void setBeforeStock(Integer beforeStock) {
        this.beforeStock = beforeStock;
    }
    
    public Integer getAfterStock() {
        return afterStock;
    }
    
    public void setAfterStock(Integer afterStock) {
        this.afterStock = afterStock;
    }
    
    public Integer getChangeAmount() {
        return changeAmount;
    }
    
    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }
    
    public StockOperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(StockOperationType operationType) {
        this.operationType = operationType;
    }
    
    public String getOperationDesc() {
        return operationDesc;
    }
    
    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }
    
    public Integer getRedisStock() {
        return redisStock;
    }
    
    public void setRedisStock(Integer redisStock) {
        this.redisStock = redisStock;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }
}