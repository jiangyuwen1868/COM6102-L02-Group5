package com.mxbc.seckill.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderItem;
import com.mxbc.seckill.entity.OrderMessage;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.UserRepository;

@Service
public class AsyncOrderService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Async("orderExecutor")
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void createOrderAsync(OrderMessage orderMessage) {
        try {
            Product product = productRepository.findById(orderMessage.getProductId()).orElse(null);
            if (product == null) {
                logger.error("商品不存在: productId={}", orderMessage.getProductId());
                return;
            }
            
            User user = userRepository.findById(orderMessage.getUserId()).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername("User-" + orderMessage.getUserId());
                user.setEmail("user" + orderMessage.getUserId() + "@example.com");
                user.setAge(25);
                user = userRepository.save(user);
                logger.info("创建新用户: userId={}", user.getId());
            }
            
            Order order = new Order();
            order.setOrderId(orderMessage.getOrderId());
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(1);
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice());
            
            order.setTotalAmount(product.getPrice());
            order.getItems().add(orderItem);
            
            Order savedOrder = orderRepository.save(order);
            logger.info("订单创建成功: orderId={}, userId={}, productId={}", 
                savedOrder.getOrderId(), user.getId(), product.getId());
        } catch (Exception e) {
            logger.error("订单创建失败: orderId={}", orderMessage.getOrderId(), e);
            throw new RuntimeException(e);
        }
    }
}