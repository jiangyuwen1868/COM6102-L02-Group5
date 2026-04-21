package com.mxbc.seckill.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mxbc.seckill.service.SeckillService;
import com.mxbc.seckill.vo.SeckillResult;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@Service
public class SeckillServiceWrapper {
    
    private final CircuitBreaker circuitBreaker;
    
    public SeckillServiceWrapper(@Qualifier("seckillCircuitBreaker") CircuitBreaker circuitBreaker) {
    	this.circuitBreaker = circuitBreaker;
    }
    
    @Autowired
    private SeckillService seckillService;
    
    public SeckillResult seckillWithCircuitBreaker(Long productId, Long userId) {
        return circuitBreaker.executeSupplier(() -> seckillService.seckill(productId, userId));
    }
}
