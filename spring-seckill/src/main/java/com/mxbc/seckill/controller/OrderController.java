package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<?> getUserOrders(HttpServletRequest request, @RequestParam(value = "status", required = false) String status) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            List<Order> orders;
            if (status != null && !status.isEmpty()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getUserOrdersByStatus(user.getId(), orderStatus);
            } else {
                orders = orderService.getUserOrders(user.getId());
            }
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的订单状态");
        } catch (Exception e) {
            logger.error("获取用户订单失败", e);
            return ResponseEntity.status(500).body("获取订单失败");
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable String orderId, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Order order = orderService.getOrderDetail(orderId);
            if (order == null) {
                return ResponseEntity.status(404).body("订单不存在");
            }
            if (!order.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).body("无权查看此订单");
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("获取订单详情失败", e);
            return ResponseEntity.status(500).body("获取订单详情失败");
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            boolean success = orderService.cancelOrder(orderId, user.getId());
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "订单取消成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body("订单取消失败，可能订单状态不允许取消");
            }
        } catch (Exception e) {
            logger.error("取消订单失败", e);
            return ResponseEntity.status(500).body("取消订单失败");
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getUserOrdersByStatus(user.getId(), orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的订单状态");
        } catch (Exception e) {
            logger.error("获取订单失败", e);
            return ResponseEntity.status(500).body("获取订单失败");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getOrderStats(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> stats = orderService.getUserOrderStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取订单统计失败", e);
            return ResponseEntity.status(500).body("获取订单统计失败");
        }
    }

    @GetMapping("/check-seckill")
    public ResponseEntity<?> checkSeckillStatus(@RequestParam Long productId, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            // 检查用户是否已经为该商品创建了订单
            Order order = orderService.getOrderByProductIdAndUserId(productId, user.getId());
            if (order != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("orderId", order.getOrderId());
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            logger.error("检查秒杀状态失败", e);
            return ResponseEntity.status(500).body("检查秒杀状态失败");
        }
    }
}
