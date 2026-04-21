package com.mxbc.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mxbc.seckill.vo.SeckillResult;

/**
 * 预售模式
 */
@Service
public class PreSaleService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 预售秒杀
     */
    public SeckillResult preSaleSeckill(Long productId, Long userId, String deposit) {
        String preSaleKey = "presale:" + productId;
        String depositKey = "deposit:" + productId + ":" + userId;
        
        // 检查是否已支付定金
        String userDeposit = (String) redisTemplate.opsForValue().get(depositKey);
        if (!deposit.equals(userDeposit)) {
            return SeckillResult.failure("请先支付定金");
        }
        
        // 执行秒杀逻辑
        return executeSeckill(productId, userId);
    }
    
    public SeckillResult executeSeckill(Long productId, Long userId) {
    	
    	return SeckillResult.failure("");
    }
}
