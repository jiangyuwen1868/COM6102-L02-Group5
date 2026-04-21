package com.mxbc.seckill.service;

import com.mxbc.seckill.common.StockPreloader;
import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockRollbackService {

    private static final Logger logger = LoggerFactory.getLogger(StockRollbackService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StockPreloader stockPreloader;

    private static final long ORDER_TIMEOUT_MINUTES = 30; // 订单超时时间30分钟

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void rollbackExpiredOrders() {
        logger.info("开始检查超时订单...");

        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING, timeoutTime);

        logger.info("发现 {} 个超时订单", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                rollbackOrderStock(order);
                logger.info("订单 {} 已超时，库存已回滚", order.getOrderId());
            } catch (Exception e) {
                logger.error("回滚订单 {} 库存失败", order.getOrderId(), e);
            }
        }
    }

    @Transactional
    public void rollbackOrderStock(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("订单 {} 状态为 {}，不需要回滚", order.getOrderId(), order.getStatus());
            return;
        }

        // 回滚库存
        order.getItems().forEach(item -> {
            // 根据商品名称查找商品
            List<Product> products = productRepository.findAll();
            Product product = products.stream()
                .filter(p -> p.getName().equals(item.getProductName()))
                .findFirst()
                .orElse(null);
            
            if (product != null) {
                int newStock = product.getStock() + item.getQuantity();
                product.setStock(newStock);
                productRepository.save(product);

                // 更新Redis库存
                stockPreloader.updateStockInRedis(product.getId(), newStock);

                // 清除用户秒杀记录
                String userKey = "seckill:user:" + order.getUserId() + ":" + product.getId();
                stringRedisTemplate.delete(userKey);

                logger.info("回滚商品 {} 库存 {}，当前库存 {}",
                        product.getId(), item.getQuantity(), newStock);
            }
        });

        // 更新订单状态为已取消
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        logger.info("订单 {} 库存回滚完成", order.getOrderId());
    }

    @Transactional
    public boolean manualRollbackStock(String orderId, Long operatorId) {
        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                logger.warn("订单 {} 不存在", orderId);
                return false;
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                logger.warn("订单 {} 状态为 {}，不允许手动回滚", orderId, order.getStatus());
                return false;
            }

            rollbackOrderStock(order);
            logger.info("操作员 {} 手动回滚订单 {} 库存", operatorId, orderId);
            return true;
        } catch (Exception e) {
            logger.error("手动回滚订单 {} 库存失败", orderId, e);
            return false;
        }
    }

    public void syncStockToRedis(Long productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                stockPreloader.updateStockInRedis(productId, product.getStock());
                logger.info("同步商品 {} 库存到Redis: {}", productId, product.getStock());
            }
        } catch (Exception e) {
            logger.error("同步商品 {} 库存到Redis失败", productId, e);
        }
    }

    public void syncAllStockToRedis() {
        try {
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                if (product.getStock() != null && product.getStock() >= 0) {
                    stockPreloader.updateStockInRedis(product.getId(), product.getStock());
                }
            }
            logger.info("同步所有商品库存到Redis完成");
        } catch (Exception e) {
            logger.error("同步所有商品库存到Redis失败", e);
        }
    }
}
