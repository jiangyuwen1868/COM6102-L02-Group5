package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.SystemMetrics;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.SeckillActivityRepository;
import com.mxbc.seckill.entity.repository.SystemMetricsRepository;
import com.mxbc.seckill.entity.repository.UserRepository;
import com.mxbc.seckill.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    @Autowired
    private SystemMetricsRepository metricsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SeckillActivityRepository activityRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AuthService authService;

    @GetMapping("/system")
    public ResponseEntity<?> getSystemMetrics(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            // 获取JVM内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            // 获取系统负载
            double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();

            // 构建系统指标数据
            Map<String, Object> systemMetrics = new HashMap<>();
            systemMetrics.put("timestamp", now);
            systemMetrics.put("jvm", Map.of(
                    "totalMemoryMb", totalMemory / (1024 * 1024),
                    "usedMemoryMb", usedMemory / (1024 * 1024),
                    "freeMemoryMb", freeMemory / (1024 * 1024),
                    "memoryUsagePercent", (double) usedMemory / totalMemory * 100
            ));
            systemMetrics.put("systemLoad", systemLoad);

            // 检查Redis连接状态
            boolean redisHealthy = true;
            try {
                stringRedisTemplate.getConnectionFactory().getConnection().ping();
            } catch (Exception e) {
                redisHealthy = false;
            }
            systemMetrics.put("redis", Map.of(
                    "healthy", redisHealthy
            ));

            return ResponseEntity.ok(systemMetrics);
        } catch (Exception e) {
            logger.error("获取系统指标失败", e);
            return ResponseEntity.status(500).body("获取系统指标失败");
        }
    }

    @GetMapping("/business")
    public ResponseEntity<?> getBusinessMetrics(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            // 获取业务统计数据
            long totalOrders = orderRepository.count();
            long totalUsers = userRepository.count();
            long totalProducts = productRepository.count();
            long totalActivities = activityRepository.count();
            long activeActivities = activityRepository.findByStatusOrderByStartTimeAsc(SeckillActivity.ActivityStatus.ACTIVE).size();

            // 计算今日订单数和销售额
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            List<Order> todayOrders = orderRepository.findByCreatedAtBetween(today, LocalDateTime.now());
            long todayOrderCount = todayOrders.size();
            double todayRevenue = todayOrders.stream()
                    .filter(order -> order.getStatus() != null && 
                            (order.getStatus() == OrderStatus.PAID || 
                             order.getStatus() == OrderStatus.SHIPPED || 
                             order.getStatus() == OrderStatus.DELIVERED))
                    .mapToDouble(order -> order.getTotalAmount().doubleValue())
                    .sum();

            // 构建业务指标数据
            Map<String, Object> businessMetrics = new HashMap<>();
            businessMetrics.put("totalOrders", totalOrders);
            businessMetrics.put("totalUsers", totalUsers);
            businessMetrics.put("totalProducts", totalProducts);
            businessMetrics.put("totalActivities", totalActivities);
            businessMetrics.put("activeActivities", activeActivities);
            businessMetrics.put("todayOrderCount", todayOrderCount);
            businessMetrics.put("todayRevenue", todayRevenue);

            return ResponseEntity.ok(businessMetrics);
        } catch (Exception e) {
            logger.error("获取业务指标失败", e);
            return ResponseEntity.status(500).body("获取业务指标失败");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistoryMetrics(
            @RequestParam(required = false, defaultValue = "7") Integer days,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<SystemMetrics> metrics = metricsRepository.findByTimestampAfterOrderByTimestampAsc(startDate);

            // 构建历史数据
            List<Map<String, Object>> historyData = new ArrayList<>();
            for (SystemMetrics metric : metrics) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("timestamp", metric.getTimestamp());
                dataPoint.put("orderCount", metric.getOrderCount());
                dataPoint.put("revenue", metric.getRevenue());
                dataPoint.put("userCount", metric.getUserCount());
                dataPoint.put("heapUsagePercent", metric.getHeapUsagePercent());
                historyData.add(dataPoint);
            }

            return ResponseEntity.ok(historyData);
        } catch (Exception e) {
            logger.error("获取历史指标失败", e);
            return ResponseEntity.status(500).body("获取历史指标失败");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardMetrics(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            // 获取系统指标
            Map<String, Object> systemMetrics = (Map<String, Object>) getSystemMetrics(request).getBody();

            // 获取业务指标
            Map<String, Object> businessMetrics = (Map<String, Object>) getBusinessMetrics(request).getBody();

            // 构建仪表盘数据
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("system", systemMetrics);
            dashboard.put("business", businessMetrics);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("获取仪表盘数据失败", e);
            return ResponseEntity.status(500).body("获取仪表盘数据失败");
        }
    }

    @PostMapping("/collect")
    public ResponseEntity<?> collectMetrics(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            // 收集并保存系统指标
            SystemMetrics metrics = new SystemMetrics();
            metrics.setTimestamp(LocalDateTime.now());

            // 业务数据
            metrics.setOrderCount((int) orderRepository.count());
            metrics.setUserCount((int) userRepository.count());
            metrics.setProductCount((int) productRepository.count());
            metrics.setActivityCount((int) activityRepository.count());
            metrics.setActiveActivityCount(activityRepository.findByStatusOrderByStartTimeAsc(SeckillActivity.ActivityStatus.ACTIVE).size());

            // 计算总收入
            List<Order> paidOrders = orderRepository.findByStatusIn(List.of(
                    OrderStatus.PAID,
                    OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED
            ));
            double totalRevenue = paidOrders.stream()
                    .mapToDouble(order -> order.getTotalAmount().doubleValue())
                    .sum();
            metrics.setRevenue(totalRevenue);

            // JVM内存数据
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double heapUsagePercent = (double) usedMemory / totalMemory * 100;
            metrics.setHeapUsedMb((double) usedMemory / (1024 * 1024));
            metrics.setHeapMaxMb((double) totalMemory / (1024 * 1024));
            metrics.setHeapUsagePercent(heapUsagePercent);

            // Redis健康状态
            boolean redisHealthy = true;
            try {
                stringRedisTemplate.getConnectionFactory().getConnection().ping();
                metrics.setRedisConnections(1); // 简化处理
            } catch (Exception e) {
                redisHealthy = false;
                metrics.setRedisConnections(0);
            }
            metrics.setRedisHealthy(redisHealthy);
            metrics.setDatabaseHealthy(true); // 简化处理

            metricsRepository.save(metrics);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "指标收集成功");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("收集指标失败", e);
            return ResponseEntity.status(500).body("收集指标失败");
        }
    }
}