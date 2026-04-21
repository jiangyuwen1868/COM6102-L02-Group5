package com.mxbc.seckill.common;

import org.springframework.stereotype.Component;

/**
 * 分段限流
 */
@Component
public class PhasedRateLimitService {
    
    public double getRateLimit(String phase) {
        switch (phase) {
            case"before":
                return 10;  // 活动前：10 QPS
            case"start":
                return 1000; // 活动开始：1000 QPS
            case"peak":
                return 5000; // 高峰期：5000 QPS
            case"end":
                return 100;  // 活动结束：100 QPS
            default:
                return 100;
        }
    }
}
