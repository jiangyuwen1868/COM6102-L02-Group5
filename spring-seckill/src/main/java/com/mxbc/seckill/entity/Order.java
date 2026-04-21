package com.mxbc.seckill.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mxbc.seckill.edb.EncryptionConverter;

@Entity
@Table(name = "orders")
public class Order {
	 @Id 
	 @GeneratedValue(strategy = GenerationType.IDENTITY) 
	 private Long id;
	 @Convert(converter = EncryptionConverter.class)
	 private String order_id;
	 @ManyToOne(fetch = FetchType.LAZY) 
	 @JoinColumn(name = "user_id") 
	 private User user;
	 @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) 
	 private List<OrderItem> items = new ArrayList<>();
	 private BigDecimal totalAmount;
	 @Enumerated(EnumType.STRING) 
	 private OrderStatus status;
	 @Column(name = "created_at") 
	 private LocalDateTime createdAt;
	 
	 public Long getId() {
		 return id;
	 }
	 public void setId(Long id) {
		 this.id = id;
	 }
	 public String getOrderId() {
		 return order_id;
	 }
	 public void setOrderId(String order_id) {
		 this.order_id = order_id;
	 }
	 @JsonIgnore
	 public User getUser() {
		 return user;
	 }
	 public void setUser(User user) {
		 this.user = user;
	 }
	 public List<OrderItem> getItems() {
		 return items;
	 }
	 public void setItems(List<OrderItem> items) {
		 this.items = items;
	 }
	 public BigDecimal getTotalAmount() {
		 return totalAmount;
	 }
	 public void setTotalAmount(BigDecimal totalAmount) {
		 this.totalAmount = totalAmount;
	 }
	 public OrderStatus getStatus() {
		 return status;
	 }
	 public void setStatus(OrderStatus status) {
		 this.status = status;
	 }
	 public LocalDateTime getCreatedAt() {
		 return createdAt;
	 }
	 public void setCreatedAt(LocalDateTime createdAt) {
		 this.createdAt = createdAt;
	 }
	 
	 public Long getUserId() {
		 return user != null ? user.getId() : null;
	 }
}
