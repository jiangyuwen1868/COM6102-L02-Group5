package com.mxbc.seckill.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.mxbc.seckill.entity.dto.StockUpdate;

/**
 * 批量操作
 */
@Service
public class BatchOperationService {
	
	@Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void batchUpdateStock(List<StockUpdate> updates) {
        String luaScript = 
            "for i = 1, #KEYS, 1 do\n" +
            "    redis.call('DECRBY', KEYS[i], ARGV[i])\n" +
            "end\n" +
            "return 'OK'";
        
        List<String> keys = updates.stream().map(u -> "stock:" + u.getProductId()).collect(Collectors.toList());
        List<String> args = updates.stream().map(u -> String.valueOf(u.getQuantity())).collect(Collectors.toList());
        
        redisTemplate.execute(new DefaultRedisScript<>(luaScript, String.class), keys, args.toArray(new String[0]));
    }
}
