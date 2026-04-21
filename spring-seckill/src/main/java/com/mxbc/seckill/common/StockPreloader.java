package com.mxbc.seckill.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.ProductRepository;

import java.util.List;

@Component
public class StockPreloader {

    private static final Logger logger = LoggerFactory.getLogger(StockPreloader.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 预热所有商品库存到Redis
     * 注意：此方法不再使用@PostConstruct，由StartupManager统一调用
     */
    public void preloadStock() {
        if (stringRedisTemplate == null) {
            logger.warn("Redis未配置，跳过库存预热");
            return;
        }

        logger.info("开始预热库存数据到Redis...");
        try {
            List<Product> products = productRepository.findAll();
            int count = 0;
            for (Product product : products) {
                if (product.getStock() != null && product.getStock() > 0) {
                    String stockKey = "stock:" + product.getId();
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
                    count++;
                }
            }
            logger.info("库存预热完成，共预热 {} 个商品", count);
        } catch (Exception e) {
            logger.error("库存预热失败", e);
            // 不抛出异常，允许系统继续运行
        }
    }

    /**
     * 预热单个商品库存
     */
    public void preloadProductStock(Long productId) {
        if (stringRedisTemplate == null) {
            return;
        }

        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                String stockKey = "stock:" + productId;
                stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
                logger.info("预热单个商品 {} 库存: {}", productId, product.getStock());
            } else {
                logger.warn("商品 {} 不存在，无法预热库存", productId);
            }
        } catch (Exception e) {
            logger.error("预热单个商品库存失败", e);
        }
    }

    /**
     * 更新Redis中的库存
     */
    public void updateStockInRedis(Long productId, int stock) {
        if (stringRedisTemplate == null) {
            return;
        }

        try {
            String stockKey = "stock:" + productId;
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
            logger.info("更新Redis中商品 {} 库存: {}", productId, stock);
        } catch (Exception e) {
            logger.error("更新Redis库存失败", e);
        }
    }

    /**
     * 获取Redis中的库存
     */
    public Integer getStockFromRedis(Long productId) {
        if (stringRedisTemplate == null) {
            return null;
        }

        try {
            String stockKey = "stock:" + productId;
            String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
            return stockStr != null ? Integer.parseInt(stockStr) : null;
        } catch (Exception e) {
            logger.error("获取Redis库存失败", e);
            return null;
        }
    }
}
