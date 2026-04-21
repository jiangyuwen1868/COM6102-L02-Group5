package com.mxbc.seckill.entity;

import java.io.Serializable;

public class OrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long productId;
    private Long userId;
    private String orderId;
    
    public OrderMessage() {}
    
    public OrderMessage(Long productId, Long userId, String orderId) {
        this.productId = productId;
        this.userId = userId;
        this.orderId = orderId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
