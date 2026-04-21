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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StockPreloader stockPreloader;

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderDetail(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public List<Order> getUserOrdersByStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    @Transactional
    public boolean cancelOrder(String orderId, Long userId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            logger.warn("订单不存在: {}", orderId);
            return false;
        }

        if (!order.getUserId().equals(userId)) {
            logger.warn("用户 {} 无权取消订单 {}", userId, orderId);
            return false;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("订单 {} 状态为 {}，不允许取消", orderId, order.getStatus());
            return false;
        }

        try {
            // 更新订单状态
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // 恢复库存
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
                            
                            // 清除用户秒杀记录，允许重新秒杀
                            String userKey = "seckill:user:" + userId + ":" + product.getId();
                            stringRedisTemplate.delete(userKey);
                            
                            logger.info("订单 {} 取消，恢复商品 {} 库存 {}，当前库存 {}",
                                    orderId, product.getId(), item.getQuantity(), newStock);
                        }
                    });

            logger.info("订单 {} 取消成功", orderId);
            return true;
        } catch (Exception e) {
            logger.error("取消订单 {} 失败", orderId, e);
            return false;
        }
    }

    public Map<String, Object> getUserOrderStats(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        stats.put("pendingOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        stats.put("paidOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count());
        stats.put("shippedOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count());
        stats.put("deliveredOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        stats.put("cancelledOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());

        double totalAmount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
        stats.put("totalSpent", totalAmount);

        return stats;
    }

    @Transactional
    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            return false;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        logger.info("订单 {} 状态更新为 {}", orderId, newStatus);
        return true;
    }

    public List<Order> getPendingOrdersBefore(LocalDateTime beforeTime) {
        return orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, beforeTime);
    }

    public Order getOrderByProductIdAndUserId(Long productId, Long userId) {
        // 查找用户的所有订单
        List<Order> orders = orderRepository.findByUserId(userId);
        // 遍历订单，查找包含指定商品的订单
        for (Order order : orders) {
            boolean hasProduct = order.getItems().stream()
                    .anyMatch(item -> {
                        // 根据商品名称查找商品
                        List<Product> products = productRepository.findAll();
                        Product product = products.stream()
                                .filter(p -> p.getName().equals(item.getProductName()))
                                .findFirst()
                                .orElse(null);
                        return product != null && product.getId().equals(productId);
                    });
            if (hasProduct) {
                return order;
            }
        }
        return null;
    }
}
