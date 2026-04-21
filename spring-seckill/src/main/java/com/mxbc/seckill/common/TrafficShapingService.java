package com.mxbc.seckill.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

@Component
public class TrafficShapingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficShapingService.class);
    
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    
    private volatile double defaultRate = 1000.0;
    
    public boolean tryAcquire(String key, int permits) {
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key, 
            k -> {
                logger.info("创建新的限流器: {}, 速率: {}/秒", key, defaultRate);
                return RateLimiter.create(defaultRate);
            });
        
        boolean acquired = rateLimiter.tryAcquire(permits, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!acquired) {
            logger.warn("限流触发: key={}, permits={}, 当前速率: {}/秒", key, permits, defaultRate);
        }
        
        return acquired;
    }
    
    public void updateRate(String key, double permitsPerSecond) {
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key,
            k -> RateLimiter.create(permitsPerSecond));
        
        rateLimiter.setRate(permitsPerSecond);
        logger.info("更新限流参数: key={}, 新速率: {}/秒", key, permitsPerSecond);
    }
    
    public void setDefaultRate(double permitsPerSecond) {
        this.defaultRate = permitsPerSecond;
        logger.info("设置默认限流速率: {}/秒", permitsPerSecond);
    }
    
    public double getDefaultRate() {
        return defaultRate;
    }
    
    public void clear() {
        rateLimiterMap.clear();
        logger.info("清空所有限流器");
    }
    
    public void remove(String key) {
        rateLimiterMap.remove(key);
        logger.info("移除限流器: {}", key);
    }
    
    public int size() {
        return rateLimiterMap.size();
    }
}
