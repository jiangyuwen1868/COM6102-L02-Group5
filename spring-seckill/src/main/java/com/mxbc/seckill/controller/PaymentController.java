package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthService authService;

    @PostMapping("/{orderId}")
    public ResponseEntity<?> processPayment(
            @PathVariable String orderId,
            @RequestBody Map<String, String> paymentInfo,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        String paymentMethod = paymentInfo.getOrDefault("paymentMethod", "alipay");

        try {
            Map<String, Object> result = paymentService.processPayment(orderId, user.getId(), paymentMethod);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("支付处理失败", e);
            return ResponseEntity.status(500).body("支付处理失败");
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> queryPaymentStatus(@PathVariable String orderId, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> result = paymentService.queryPaymentStatus(orderId, user.getId());
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("查询支付状态失败", e);
            return ResponseEntity.status(500).body("查询失败");
        }
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refundOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> refundInfo,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        String reason = refundInfo.getOrDefault("reason", "用户申请退款");

        try {
            Map<String, Object> result = paymentService.refundOrder(orderId, user.getId(), reason);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("退款处理失败", e);
            return ResponseEntity.status(500).body("退款处理失败");
        }
    }
}
