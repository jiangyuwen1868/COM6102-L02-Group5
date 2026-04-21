package com.mxbc.seckill.common;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mxbc.seckill.vo.SeckillResult;

@Component
public class SeckillLuaScript {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String SECKILL_SCRIPT = 
	"local stockKey = KEYS[1]\n" +
	"local orderId = ARGV[1]\n" +
	"local userId = ARGV[2]\n" +
	"\n" +
	"-- Check if user has already participated\n" +
	"local userKey = 'seckill:user:' .. ARGV[2] .. ':' .. KEYS[1]\n" +
	"local exists = redis.call('GET', userKey)\n" +
	"if exists then\n" +
	"  return '{\"code\":3}'\n" +
	"end\n" +
	"\n" +
	"-- Check stock\n" +
	"local stock = redis.call('GET', stockKey)\n" +
	"if not stock then\n" +
	"  return '{\"code\":1}'\n" +
	"end\n" +
	"\n" +
	"stock = tonumber(stock)\n" +
	"if stock <= 0 then\n" +
	"  return '{\"code\":2}'\n" +
	"end\n" +
	"\n" +
	"-- Decrement stock\n" +
	"redis.call('DECR', stockKey)\n" +
	"\n" +
	"-- Record user seckill\n" +
	"redis.call('SET', userKey, '1', 'EX', 86400)\n" +
	"\n" +
	"local remainingStock = stock - 1\n" +
	"return '{\"code\":0,\"orderId\":\"' .. orderId .. '\",\"stock\":' .. remainingStock .. '}'";

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	public SeckillResult executeSeckill(Long productId, Long userId) {
		String stockKey = "stock:" + productId;
        String[] keys = new String[]{stockKey};
        String orderId = "ORD-" + System.currentTimeMillis() + "-" + userId + "-" + productId;
        String[] args = new String[]{orderId, String.valueOf(userId)};
        
        try {
            RedisScript<String> script = new DefaultRedisScript<>(SECKILL_SCRIPT, String.class);
            String result = stringRedisTemplate.execute(script, Arrays.asList(keys), args);
            
            logger.info("秒杀执行结果: {}", result);
            
            return parseResult(result);
        } catch (Exception e) {
        	logger.error("秒杀执行失败", e);
            return SeckillResult.failure("系统异常，请稍后再试");
        }
	}
	
	private SeckillResult parseResult(String result) {
        try {
            return JSONObject.parseObject(result, SeckillResult.class);
        } catch (Exception e) {
            logger.error("解析秒杀结果失败: {}", result, e);
            return SeckillResult.failure("系统异常，请稍后再试");
        }
    }
}
