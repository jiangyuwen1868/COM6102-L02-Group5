package com.mxbc.seckill.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.ProductRepository;

/**
 * 在秒杀活动开始前，将商品库存加载到Redis
 */
@Service
public class StockPreheatService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 预热库存
     */
    public void preheatStock(Long productId) {
        // 从数据库获取商品信息
        Optional<Product> product = productRepository.findById(productId);
        
        // 将库存信息加载到Redis
        String stockKey = "stock:" + productId;
        redisTemplate.opsForValue().set(stockKey, product.get().getStock(), Duration.ofHours(2));
        
        // 设置商品信息
        String productKey = "product:" + productId;
        redisTemplate.opsForValue().set(productKey, product, Duration.ofHours(2));
        
        // 初始化秒杀状态
        String seckillStatusKey = "seckill:status:" + productId;
        redisTemplate.opsForValue().set(seckillStatusKey, "ready", Duration.ofHours(2));
    }
    
    /**
     * 批量预热库存
     */
    public void batchPreheatStock(List<Long> productIds) {
        productIds.parallelStream().forEach(this::preheatStock);
    }
}
