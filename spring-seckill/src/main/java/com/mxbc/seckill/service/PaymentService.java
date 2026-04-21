package com.mxbc.seckill.service;

import com.mxbc.seckill.controller.WebSocketController;
import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketController webSocketController;

    private final Random random = new Random();

    @Transactional
    public Map<String, Object> processPayment(String orderId, Long userId, String paymentMethod) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权支付此订单");
                return result;
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                result.put("success", false);
                result.put("message", "订单状态不允许支付");
                return result;
            }

            // 模拟支付处理（实际项目中应调用第三方支付接口）
            boolean paymentSuccess = simulatePayment(order, paymentMethod);

            if (paymentSuccess) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                // 发送WebSocket通知
                webSocketController.sendSeckillResult(userId, true, "支付成功");

                result.put("success", true);
                result.put("message", "支付成功");
                result.put("orderId", orderId);
                result.put("paidAt", LocalDateTime.now().toString());

                logger.info("订单 {} 支付成功，用户 {}", orderId, userId);
            } else {
                result.put("success", false);
                result.put("message", "支付失败，请重试");

                logger.warn("订单 {} 支付失败，用户 {}", orderId, userId);
            }

            return result;
        } catch (Exception e) {
            logger.error("支付处理失败", e);
            result.put("success", false);
            result.put("message", "支付处理异常");
            return result;
        }
    }

    private boolean simulatePayment(Order order, String paymentMethod) {
        // 模拟支付处理，实际项目中应调用支付宝、微信支付等接口
        // 这里模拟95%的成功率
        try {
            Thread.sleep(500); // 模拟网络延迟
            return random.nextDouble() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Transactional
    public Map<String, Object> queryPaymentStatus(String orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权查询此订单");
                return result;
            }

            result.put("success", true);
            result.put("orderId", orderId);
            result.put("status", order.getStatus().name());
            result.put("statusDesc", order.getStatus().getDescription());

            return result;
        } catch (Exception e) {
            logger.error("查询支付状态失败", e);
            result.put("success", false);
            result.put("message", "查询失败");
            return result;
        }
    }

    @Transactional
    public Map<String, Object> refundOrder(String orderId, Long userId, String reason) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权退款此订单");
                return result;
            }

            if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED) {
                result.put("success", false);
                result.put("message", "订单状态不允许退款");
                return result;
            }

            // 模拟退款处理
            boolean refundSuccess = simulateRefund(order, reason);

            if (refundSuccess) {
                order.setStatus(OrderStatus.REFUNDED);
                orderRepository.save(order);

                // 恢复库存
                orderService.cancelOrder(orderId, userId);

                result.put("success", true);
                result.put("message", "退款成功");
                result.put("orderId", orderId);
                result.put("refundAmount", order.getTotalAmount());

                logger.info("订单 {} 退款成功，用户 {}，原因 {}", orderId, userId, reason);
            } else {
                result.put("success", false);
                result.put("message", "退款失败");
            }

            return result;
        } catch (Exception e) {
            logger.error("退款处理失败", e);
            result.put("success", false);
            result.put("message", "退款处理异常");
            return result;
        }
    }

    private boolean simulateRefund(Order order, String reason) {
        // 模拟退款处理，实际项目中应调用第三方支付接口
        try {
            Thread.sleep(300); // 模拟网络延迟
            return random.nextDouble() < 0.98; // 98%的退款成功率
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
