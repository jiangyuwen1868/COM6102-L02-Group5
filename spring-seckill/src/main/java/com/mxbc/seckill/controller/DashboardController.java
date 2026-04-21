package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AuthService authService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘统计失败", e);
            return ResponseEntity.status(500).body("获取统计数据失败");
        }
    }

    @GetMapping("/realtime")
    public ResponseEntity<?> getRealtimeStats(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> stats = dashboardService.getRealtimeStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取实时统计失败", e);
            return ResponseEntity.status(500).body("获取实时数据失败");
        }
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> result = dashboardService.getTopProducts();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取热门商品失败", e);
            return ResponseEntity.status(500).body("获取热门商品失败");
        }
    }

    @GetMapping("/system-health")
    public ResponseEntity<?> getSystemHealth(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> health = dashboardService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("获取系统健康状态失败", e);
            return ResponseEntity.status(500).body("获取系统健康状态失败");
        }
    }
}
