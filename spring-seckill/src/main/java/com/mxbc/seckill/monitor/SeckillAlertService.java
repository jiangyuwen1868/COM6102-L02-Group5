package com.mxbc.seckill.monitor;

import org.springframework.stereotype.Component;

/**
 * 异常告警
 */
@Component
public class SeckillAlertService {
    
    public void checkStockAlert(Long productId, int currentStock) {
        if (currentStock < 100) { // 库存不足告警
//            alertService.sendAlert("库存不足告警", 
//                String.format("商品[%d]库存不足，当前库存: %d", productId, currentStock));
        }
    }
}
