package com.mxbc.seckill.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 库存分片
 */
@Component
public class ShardedStockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 分片库存扣减
     */
    public boolean deductStock(Long productId, int quantity) {
        String baseKey = "stock:" + productId;
        int shardCount = 10; // 分片数量
        
        // 计算需要扣减的分片
        int remaining = quantity;
        for (int i = 0; i < shardCount && remaining > 0; i++) {
            String shardKey = baseKey + ":shard:" + i;
            Long currentStock = redisTemplate.opsForValue().increment(shardKey, -remaining);
            
            if (currentStock < 0) {
                // 回滚已扣减的库存
                redisTemplate.opsForValue().increment(shardKey, remaining + currentStock);
                remaining = -currentStock.intValue();
            } else {
                remaining = 0;
            }
        }
        
        return remaining == 0;
    }
}
