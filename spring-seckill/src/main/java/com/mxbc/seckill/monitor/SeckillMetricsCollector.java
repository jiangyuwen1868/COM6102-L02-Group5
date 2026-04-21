package com.mxbc.seckill.monitor;


import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

/**
 * 关键监控指标
 */
@Component
public class SeckillMetricsCollector {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MeterRegistry meterRegistry;

    // 构造函数注入MeterRegistry（Spring Boot自动配置）
    public SeckillMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录秒杀尝试次数（成功/失败）
     * @param productId 商品ID（不可为null）
     * @param success 是否成功
     */
    public void recordSeckillAttempt(Long productId, boolean success) {
        // 入参校验（避免空指针和非法值）
        Assert.notNull(productId, "productId must not be null");
        if (productId <= 0) {
        	logger.warn("Invalid productId: {}", productId);
            return;
        }

        // 复用Counter实例（避免重复创建）
        Counter counter = meterRegistry.find("seckill_attempts_total")
                .tags("product_id", productId.toString(), "result", success ? "success" : "failure")
                .counter();

        if (counter == null) {
            // 首次创建Counter
            counter = Counter.builder("seckill_attempts_total")
                    .tags(Arrays.asList(
                            Tag.of("product_id", productId.toString()),
                            Tag.of("result", success ? "success" : "failure")
                    ))
                    .description("Total number of seckill attempts")
                    .register(meterRegistry);
        }

        counter.increment(); // 计数+1
    }

    /**
     * 记录并动态更新商品库存（支持实时刷新）
     * @param productId 商品ID（不可为null）
     * @param stock 库存数量（需>=0）
     */
    public void recordStock(Long productId, int stock) {
        // 入参校验
        Assert.notNull(productId, "productId must not be null");
        if (stock < 0) {
        	logger.warn("Invalid stock value: {} for product {}", stock, productId);
            return;
        }

        // 使用AtomicInteger存储库存，支持Gauge动态更新
        String gaugeName = "seckill_stock";
        String productTag = productId.toString();

        // 查找已存在的Gauge（通过AtomicInteger的引用判断）
        AtomicInteger stockHolder = (AtomicInteger) meterRegistry.find(gaugeName)
                .tag("product_id", productTag)
                .gauge();

        if (stockHolder == null) {
            // 首次创建Gauge，绑定AtomicInteger
            stockHolder = new AtomicInteger(stock);
            Gauge.builder(gaugeName, stockHolder, AtomicInteger::get)
                    .tag("product_id", productTag)
                    .description("Current stock of seckill product")
                    .register(meterRegistry);
        } else {
            // 更新已有Gauge的库存值
            stockHolder.set(stock);
        }
    }
}

