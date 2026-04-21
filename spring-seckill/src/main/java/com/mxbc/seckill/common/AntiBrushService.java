package com.mxbc.seckill.common;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 防刷机制
 */
@Component
public class AntiBrushService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean isBrushing(String ip, Long userId) {
        String ipKey = "brush:ip:" + ip;
        String userKey = "brush:user:" + userId;
        
        // 检查IP请求频率
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount > 50) { // 10秒内超过50次请求
            redisTemplate.expire(ipKey, Duration.ofSeconds(10));
            return true;
        }
        
        // 检查用户请求频率
        Long userCount = redisTemplate.opsForValue().increment(userKey);
        if (userCount > 20) { // 10秒内超过20次请求
            redisTemplate.expire(userKey, Duration.ofSeconds(10));
            return true;
        }
        
        // 设置过期时间
        redisTemplate.expire(ipKey, Duration.ofSeconds(10));
        redisTemplate.expire(userKey, Duration.ofSeconds(10));
        
        return false;
    }
}
