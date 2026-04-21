package com.mxbc.seckill.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.ProductRepository;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductInfo(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            result.put("code", -1);
            result.put("message", "商品不存在");
            return ResponseEntity.ok(result);
        }
        
        String stockKey = "stock:" + productId;
        String statusKey = "seckill:status:" + productId;
        
        Object stock = redisTemplate.opsForValue().get(stockKey);
        Object status = redisTemplate.opsForValue().get(statusKey);
        
        String activityStatus = determineActivityStatus(product, status);
        
        result.put("code", 0);
        result.put("data", Map.of(
            "id", product.getId(),
            "name", product.getName(),
            "description", product.getDescription(),
            "price", product.getPrice(),
            "originalPrice", product.getOriginalPrice(),
            "stock", stock != null ? stock : product.getStock(),
            "status", activityStatus,
            "startTime", product.getStartTime() != null ? product.getStartTime().toString() : null,
            "endTime", product.getEndTime() != null ? product.getEndTime().toString() : null,
            "imageUrl", product.getImageUrl()
        ));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getProductList() {
        Map<String, Object> result = new HashMap<>();
        
        Iterable<Product> products = productRepository.findAll();
        List<Map<String, Object>> productList = new ArrayList<>();
        
        for (Product product : products) {
            String stockKey = "stock:" + product.getId();
            String statusKey = "seckill:status:" + product.getId();
            
            Object stock = redisTemplate.opsForValue().get(stockKey);
            Object status = redisTemplate.opsForValue().get(statusKey);
            
            String activityStatus = determineActivityStatus(product, status);
            
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("description", product.getDescription());
            productData.put("price", product.getPrice());
            productData.put("originalPrice", product.getOriginalPrice());
            productData.put("stock", stock != null ? stock : product.getStock());
            productData.put("status", activityStatus);
            productData.put("startTime", product.getStartTime() != null ? product.getStartTime().toString() : null);
            productData.put("endTime", product.getEndTime() != null ? product.getEndTime().toString() : null);
            productData.put("imageUrl", product.getImageUrl());
            
            productList.add(productData);
        }
        
        result.put("code", 0);
        result.put("data", productList);
        
        return ResponseEntity.ok(result);
    }
    
    private String determineActivityStatus(Product product, Object redisStatus) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = product.getStartTime();
        LocalDateTime endTime = product.getEndTime();
        
        if (startTime == null || endTime == null) {
            return redisStatus != null ? redisStatus.toString() : "inactive";
        }
        
        if (now.isBefore(startTime)) {
            return "upcoming";
        } else if (now.isAfter(endTime)) {
            return "ended";
        } else {
            return "active";
        }
    }
}