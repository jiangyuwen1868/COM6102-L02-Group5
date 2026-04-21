package com.mxbc.seckill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
	 @Id 
	 @GeneratedValue(strategy = GenerationType.IDENTITY) 
	 private Long id;	 private String name;
	 private String description;
	 @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private BigDecimal originalPrice;
	 private Integer stock;
	 private String category;
	 private String imageUrl;
	 @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    @Column(name = "created_at") 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Column(name = "updated_at") 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
	 public Long getId() {
		 return id;
	 }
	 public void setId(Long id) {
		 this.id = id;
	 }
	 public String getName() {
		 return name;
	 }
	 public void setName(String name) {
		 this.name = name;
	 }
	 public String getDescription() {
		 return description;
	 }
	 public void setDescription(String description) {
		 this.description = description;
	 }
	 public BigDecimal getPrice() {
		 return price;
	 }
	 public void setPrice(BigDecimal price) {
		 this.price = price;
	 }
	 public BigDecimal getOriginalPrice() {
		 return originalPrice;
	 }
	 public void setOriginalPrice(BigDecimal originalPrice) {
		 this.originalPrice = originalPrice;
	 }
	 public Integer getStock() {
		 return stock;
	 }
	 public void setStock(Integer stock) {
		 this.stock = stock;
	 }
	 public String getCategory() {
		 return category;
	 }
	 public void setCategory(String category) {
		 this.category = category;
	 }
	 public String getImageUrl() {
		 return imageUrl;
	 }
	 public void setImageUrl(String imageUrl) {
		 this.imageUrl = imageUrl;
	 }
	 public LocalDateTime getStartTime() {
		 return startTime;
	 }
	 public void setStartTime(LocalDateTime startTime) {
		 this.startTime = startTime;
	 }
	 public LocalDateTime getEndTime() {
		 return endTime;
	 }
	 public void setEndTime(LocalDateTime endTime) {
		 this.endTime = endTime;
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
	 
}
