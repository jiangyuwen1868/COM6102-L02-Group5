package com.mxbc.seckill.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.UserRepository;
import com.mxbc.seckill.service.SeckillService;
import com.mxbc.seckill.vo.SeckillResult;

@RestController
@RequestMapping("/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SeckillService seckillService;
    
    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initTestData() {
        Map<String, String> result = new HashMap<>();
        
        try {
            redisTemplate.opsForValue().set("stock:1", 100);
            redisTemplate.opsForValue().set("stock:2", 50);
            redisTemplate.opsForValue().set("stock:3", 200);
            
            redisTemplate.opsForValue().set("seckill:status:1", "active");
            redisTemplate.opsForValue().set("seckill:status:2", "active");
            redisTemplate.opsForValue().set("seckill:status:3", "active");
            
            com.mxbc.seckill.entity.Product product1 = productRepository.findById(1L).orElse(null);
            if (product1 != null) {
                product1.setStock(100);
                productRepository.save(product1);
            }
            
            com.mxbc.seckill.entity.Product product2 = productRepository.findById(2L).orElse(null);
            if (product2 != null) {
                product2.setStock(50);
                productRepository.save(product2);
            }
            
            com.mxbc.seckill.entity.Product product3 = productRepository.findById(3L).orElse(null);
            if (product3 != null) {
                product3.setStock(200);
                productRepository.save(product3);
            }
            
            result.put("status", "success");
            result.put("message", "测试数据初始化成功");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "初始化失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    public java.util.concurrent.CompletableFuture<ResponseEntity<Map<String, Object>>> rateLimiterFallback(Long userId, Long productId, Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "rate_limited");
        result.put("message", "参与人数过多，请稍后再试");
        result.put("error", "RateLimiter");
        logger.warn("触发限流降级: userId={}, productId={}", userId, productId);
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> ResponseEntity.ok(result));
    }

    public java.util.concurrent.CompletableFuture<ResponseEntity<Map<String, Object>>> circuitBreakerFallback(Long userId, Long productId, Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "circuit_open");
        result.put("message", "系统繁忙，请稍后重试");
        result.put("error", "CircuitBreaker");
        logger.error("触发熔断降级: userId={}, productId={}", userId, productId, e);
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> ResponseEntity.ok(result));
    }
    
    @GetMapping("/stock/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        String stockKey = "stock:" + productId;
        Object redisStock = redisTemplate.opsForValue().get(stockKey);
        
        int dbStock = 0;
        try {
            com.mxbc.seckill.entity.Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                dbStock = product.getStock();
            }
        } catch (Exception e) {
            logger.error("获取数据库库存失败", e);
        }
        
        result.put("productId", productId);
        result.put("redisStock", redisStock);
        result.put("dbStock", dbStock);
        result.put("consistent", redisStock != null && dbStock == Integer.parseInt(redisStock.toString()));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status/{productId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        String statusKey = "seckill:status:" + productId;
        Object status = redisTemplate.opsForValue().get(statusKey);
        result.put("productId", productId);
        result.put("status", status);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", orderRepository.count());
        result.put("orders", orderRepository.findAll());
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clearOrders")
    public ResponseEntity<Map<String, String>> clearOrders() {
        Map<String, String> result = new HashMap<>();
        try {
            orderRepository.deleteAll();
            result.put("status", "success");
            result.put("message", "订单清除成功");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "订单清除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user/{userId}/orders")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        long count = orderRepository.countByUserId(userId);
        logger.info("查询用户 {} 的订单数量: {}", userId, count);
        result.put("userId", userId);
        result.put("orderCount", count);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clearUserSeckill/{userId}/{productId}")
    public ResponseEntity<Map<String, String>> clearUserSeckill(@PathVariable Long userId, @PathVariable Long productId) {
        Map<String, String> result = new HashMap<>();
        try {
            String userKey = "seckill:user:" + userId + ":" + productId;
            Boolean deleted = redisTemplate.delete(userKey);
            result.put("status", "success");
            result.put("message", "用户秒杀记录清除成功");
            result.put("deleted", deleted.toString());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "清除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clearBrushRecords/{userId}")
    public ResponseEntity<Map<String, String>> clearBrushRecords(@PathVariable Long userId) {
        Map<String, String> result = new HashMap<>();
        try {
            String userKey = "brush:user:" + userId;
            Boolean deleted = redisTemplate.delete(userKey);
            result.put("status", "success");
            result.put("message", "用户刷单记录清除成功");
            result.put("deleted", deleted.toString());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "清除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/testSeckill/{userId}/{productId}")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "seckillRateLimiter", fallbackMethod = "rateLimiterFallback")
    @io.github.resilience4j.timelimiter.annotation.TimeLimiter(name = "seckillTimeLimiter")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "seckillCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public java.util.concurrent.CompletableFuture<ResponseEntity<Map<String, Object>>> testSeckill(@PathVariable Long userId, @PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始测试秒杀，用户ID: {}, 商品ID: {}", userId, productId);
            
            SeckillResult seckillResult = seckillService.seckill(productId, userId);
            
            logger.info("秒杀结果: code={}, message={}, orderId={}", 
                seckillResult.getCode(), seckillResult.getMessage(), seckillResult.getOrderId());
            
            result.put("status", "success");
            result.put("code", seckillResult.getCode());
            result.put("message", seckillResult.getMessage());
            result.put("orderId", seckillResult.getOrderId());
        } catch (Exception e) {
            logger.error("测试秒杀失败", e);
            result.put("status", "error");
            result.put("message", "测试失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return java.util.concurrent.CompletableFuture.completedFuture(ResponseEntity.ok(result));
    }
    
    @PostMapping("/testRedis/{key}/{value}")
    public ResponseEntity<Map<String, Object>> testRedis(@PathVariable String key, @PathVariable String value) {
        Map<String, Object> result = new HashMap<>();
        try {
            stringRedisTemplate.opsForValue().set("test:" + key, value);
            String retrieved = stringRedisTemplate.opsForValue().get("test:" + key);
            
            result.put("status", "success");
            result.put("key", "test:" + key);
            result.put("setValue", value);
            result.put("getValue", retrieved);
            result.put("match", value.equals(retrieved));
        } catch (Exception e) {
            logger.error("Redis测试失败", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/getSession/{token}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sessionKey = "session:" + token;
            String userId = stringRedisTemplate.opsForValue().get(sessionKey);
            
            result.put("status", "success");
            result.put("token", token);
            result.put("sessionKey", sessionKey);
            result.put("userId", userId);
            result.put("exists", userId != null && !userId.isEmpty());
        } catch (Exception e) {
            logger.error("获取session失败", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/testSession/{userId}")
    public ResponseEntity<Map<String, Object>> testSession(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = "test-token-" + System.currentTimeMillis();
            String sessionKey = "session:" + token;
            String userIdStr = String.valueOf(userId);
            
            logger.info("存储session: sessionKey={}, userIdStr={}", sessionKey, userIdStr);
            stringRedisTemplate.opsForValue().set(sessionKey, userIdStr);
            stringRedisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(3600 * 24));
            
            String retrieved = stringRedisTemplate.opsForValue().get(sessionKey);
            
            logger.info("获取session: sessionKey={}, retrieved={}", sessionKey, retrieved);
            
            result.put("status", "success");
            result.put("token", token);
            result.put("sessionKey", sessionKey);
            result.put("setUserId", userIdStr);
            result.put("getUserId", retrieved);
            result.put("match", userIdStr.equals(retrieved));
        } catch (Exception e) {
            logger.error("测试session失败", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    private static int failureCount = 0;

    @GetMapping("/testCircuitBreaker")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "seckillCircuitBreaker", fallbackMethod = "circuitBreakerFallbackSimple")
    public java.util.concurrent.CompletableFuture<ResponseEntity<Map<String, Object>>> testCircuitBreaker() {
        Map<String, Object> result = new HashMap<>();
        failureCount++;
        logger.info("测试熔断器，失败计数: {}", failureCount);
        
        if (failureCount <= 10) {
            throw new RuntimeException("模拟服务失败");
        }
        
        result.put("status", "success");
        result.put("message", "服务正常");
        result.put("failureCount", failureCount);
        return java.util.concurrent.CompletableFuture.completedFuture(ResponseEntity.ok(result));
    }

    public java.util.concurrent.CompletableFuture<ResponseEntity<Map<String, Object>>> circuitBreakerFallbackSimple(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "circuit_open");
        result.put("message", "熔断器已打开，服务降级");
        result.put("error", "CircuitBreaker");
        logger.error("熔断器降级触发", e);
        return java.util.concurrent.CompletableFuture.completedFuture(ResponseEntity.ok(result));
    }

    @GetMapping("/resetCircuitBreaker")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker() {
        Map<String, Object> result = new HashMap<>();
        failureCount = 0;
        result.put("status", "success");
        result.put("message", "熔断器测试计数已重置");
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clearSeckillRecords")
    public ResponseEntity<Map<String, Object>> clearSeckillRecords() {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> keys = stringRedisTemplate.keys("seckill:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                result.put("status", "success");
                result.put("message", "已清除 " + keys.size() + " 条秒杀记录");
                result.put("count", keys.size());
            } else {
                result.put("status", "success");
                result.put("message", "没有找到秒杀记录");
                result.put("count", 0);
            }
        } catch (Exception e) {
            logger.error("清除秒杀记录失败", e);
            result.put("status", "error");
            result.put("message", "清除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/setProductTime")
    public ResponseEntity<Map<String, Object>> setProductTime(@RequestParam Long productId, @RequestParam(defaultValue = "0") Integer offsetMinutes) {
        Map<String, Object> result = new HashMap<>();
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                result.put("status", "error");
                result.put("message", "商品不存在");
                return ResponseEntity.ok(result);
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.plusMinutes(offsetMinutes != null && offsetMinutes > 0 ? offsetMinutes : 0);
            LocalDateTime endTime = startTime.plusHours(2);
            
            product.setStartTime(startTime);
            product.setEndTime(endTime);
            productRepository.save(product);
            
            result.put("status", "success");
            result.put("message", "已设置商品活动时间");
            result.put("startTime", startTime.toString());
            result.put("endTime", endTime.toString());
        } catch (Exception e) {
            logger.error("设置商品活动时间失败", e);
            result.put("status", "error");
            result.put("message", "设置失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
