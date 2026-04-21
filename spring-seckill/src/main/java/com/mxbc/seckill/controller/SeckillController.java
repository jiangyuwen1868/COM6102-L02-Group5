package com.mxbc.seckill.controller;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mxbc.seckill.common.AntiBrushService;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.SeckillService;
import com.mxbc.seckill.vo.SeckillResult;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RestController
@RequestMapping("/api")
public class SeckillController {
	
	private static final Logger logger = LoggerFactory.getLogger(SeckillController.class);
	
	@Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private SeckillService seckillService;
    
    @Autowired
    private AntiBrushService antiBrushService;
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/seckill/{productId}")
    @RateLimiter(name = "seckillRateLimiter", fallbackMethod = "rateLimiterFallback")
    @TimeLimiter(name = "seckillTimeLimiter")
    @CircuitBreaker(name = "seckillCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public CompletableFuture<ResponseEntity<SeckillResult>> seckill(
            @PathVariable Long productId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Real-IP", required = false) String ip,
            @RequestParam String requestId) {
        
        User user = authService.getUserByToken(token);
        if (user == null) {
            return CompletableFuture.completedFuture(ResponseEntity.ok(SeckillResult.failure("请先登录")));
        }
        
        if (ip == null) {
            ip = "127.0.0.1";
        }
        
        // 检查请求ID是否重复
        String requestKey = "request:" + requestId;
        if (redisTemplate.hasKey(requestKey)) {
            return CompletableFuture.completedFuture(ResponseEntity.ok(SeckillResult.failure("请勿重复提交")));
        }
        
        // 设置请求ID过期时间
        redisTemplate.opsForValue().set(requestKey, "1", Duration.ofMinutes(5));
        
        try {
            // 执行秒杀
            SeckillResult result = seckillService.seckill(productId, user.getId());
            return CompletableFuture.completedFuture(ResponseEntity.ok(result));
        } catch (Exception e) {
            logger.error("秒杀执行异常: productId={}, userId={}", productId, user.getId(), e);
            // 检查是否已经秒杀成功（通过Redis用户标记）
            String userKey = "seckill:user:" + user.getId() + ":stock:" + productId;
            if (redisTemplate.hasKey(userKey)) {
                // 用户已经参与过秒杀，返回相应提示
                return CompletableFuture.completedFuture(ResponseEntity.ok(SeckillResult.failure("您已参与过本次秒杀，每人限购一件")));
            }
            return CompletableFuture.completedFuture(ResponseEntity.ok(SeckillResult.failure("系统异常，请稍后再试")));
        }
    }
    
    public CompletableFuture<ResponseEntity<SeckillResult>> rateLimiterFallback(Long productId, String token, String ip, String requestId, Exception e) {
        logger.warn("Rate limiter fallback triggered: productId={}, ip={}, error={}", productId, ip, e.getMessage());
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(SeckillResult.failure("参与人数过多，请稍后再试")));
    }

    public CompletableFuture<ResponseEntity<SeckillResult>> circuitBreakerFallback(Long productId, String token, String ip, String requestId, Exception e) {
    	logger.error("秒杀服务熔断，productId:{}, token:{}", productId, token, e);
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(SeckillResult.failure("系统繁忙，请稍后重试")));
    }
}
