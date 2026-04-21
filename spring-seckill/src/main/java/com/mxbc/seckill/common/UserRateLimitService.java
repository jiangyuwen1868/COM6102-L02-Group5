package com.mxbc.seckill.common;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * 用户限流
 */
@Component
public class UserRateLimitService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 用户请求限流
     */
    public boolean isAllowed(Long userId, String action, int limit, int windowSeconds) {
        String key = "rate_limit:" + userId + ":" + action;
        String luaScript = 
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local current = redis.call('GET', key)\n" +
            "if current == false then\n" +
            "    redis.call('SET', key, 1)\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "    return 1\n" +
            "end\n" +
            "current = tonumber(current)\n" +
            "if current < limit then\n" +
            "    redis.call('INCR', key)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";
        
        RedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = (Long) redisTemplate.execute(script, Arrays.asList(key), 
            String.valueOf(limit), String.valueOf(windowSeconds));
        
        return result == 1;
    }
}
