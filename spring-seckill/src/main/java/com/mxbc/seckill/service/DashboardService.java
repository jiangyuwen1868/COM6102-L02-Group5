package com.mxbc.seckill.service;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.SeckillActivityRepository;
import com.mxbc.seckill.entity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeckillActivityRepository activityRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 订单统计
            List<Order> allOrders = orderRepository.findAll();
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long paidOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
            long shippedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
            long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
            long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

            // 销售额统计
            double totalRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PAID || 
                               o.getStatus() == OrderStatus.SHIPPED || 
                               o.getStatus() == OrderStatus.DELIVERED)
                    .filter(o -> o.getTotalAmount() != null)
                    .mapToDouble(o -> o.getTotalAmount().doubleValue())
                    .sum();

            // 用户统计
            long totalUsers = userRepository.count();

            // 商品统计
            long totalProducts = productRepository.count();
            List<Product> products = productRepository.findAll();
            long lowStockProducts = products.stream().filter(p -> p.getStock() < 10).count();

            // 活动统计
            long totalActivities = activityRepository.count();
            List<SeckillActivity> activities = activityRepository.findAll();
            long activeActivities = activities.stream().filter(a -> a.getStatus() == SeckillActivity.ActivityStatus.ACTIVE).count();
            long upcomingActivities = activities.stream().filter(a -> a.getStatus() == SeckillActivity.ActivityStatus.UPCOMING).count();

            stats.put("success", true);
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("paidOrders", paidOrders);
            stats.put("shippedOrders", shippedOrders);
            stats.put("deliveredOrders", deliveredOrders);
            stats.put("cancelledOrders", cancelledOrders);
            stats.put("totalRevenue", totalRevenue);
            stats.put("totalUsers", totalUsers);
            stats.put("totalProducts", totalProducts);
            stats.put("lowStockProducts", lowStockProducts);
            stats.put("totalActivities", totalActivities);
            stats.put("activeActivities", activeActivities);
            stats.put("upcomingActivities", upcomingActivities);

        } catch (Exception e) {
            logger.error("获取仪表盘统计失败", e);
            stats.put("success", false);
            stats.put("message", "获取统计数据失败");
        }

        return stats;
    }

    public Map<String, Object> getRealtimeStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minus(1, ChronoUnit.HOURS);

            // 最近一小时的订单
            List<Order> recentOrders = orderRepository.findByCreatedAtBetween(oneHourAgo, now);
            long recentOrderCount = recentOrders.size();
            double recentRevenue = recentOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PAID)
                    .mapToDouble(o -> o.getTotalAmount().doubleValue())
                    .sum();

            // 当前进行中的活动
            List<SeckillActivity> activeActivities = activityRepository.findByStatusOrderByStartTimeAsc(SeckillActivity.ActivityStatus.ACTIVE);

            // Redis连接数（近似值）
            Long redisConnections = 0L;
            try {
                Properties info = stringRedisTemplate.execute((RedisCallback<Properties>) connection -> {
                    return connection.info("clients");
                });
                if (info != null) {
                    String connectedClients = info.getProperty("connected_clients");
                    if (connectedClients != null) {
                        redisConnections = Long.parseLong(connectedClients);
                    }
                }
            } catch (Exception e) {
                logger.warn("获取Redis连接数失败", e);
            }

            stats.put("success", true);
            stats.put("recentOrderCount", recentOrderCount);
            stats.put("recentRevenue", recentRevenue);
            stats.put("activeActivities", activeActivities.size());
            stats.put("redisConnections", redisConnections != null ? redisConnections : 0);
            stats.put("timestamp", now.toString());

        } catch (Exception e) {
            logger.error("获取实时统计失败", e);
            stats.put("success", false);
            stats.put("message", "获取实时数据失败");
        }

        return stats;
    }

    public Map<String, Object> getTopProducts() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Order> allOrders = orderRepository.findAll();
            
            // 统计每个商品的销售数量
            Map<String, Integer> productSales = new HashMap<>();
            for (Order order : allOrders) {
                if (order.getStatus() == OrderStatus.PAID || 
                    order.getStatus() == OrderStatus.SHIPPED || 
                    order.getStatus() == OrderStatus.DELIVERED) {
                    order.getItems().forEach(item -> {
                        String productName = item.getProductName();
                        if (productName != null) {
                            productSales.merge(productName, item.getQuantity(), Integer::sum);
                        }
                    });
                }
            }

            // 获取Top 10商品
            List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> productInfo = new HashMap<>();
                        productInfo.put("productName", entry.getKey());
                        productInfo.put("salesCount", entry.getValue());
                        return productInfo;
                    })
                    .collect(Collectors.toList());

            result.put("success", true);
            result.put("topProducts", topProducts);

        } catch (Exception e) {
            logger.error("获取热门商品失败", e);
            result.put("success", false);
            result.put("message", "获取热门商品失败");
        }

        return result;
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // JVM内存信息
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

            long heapUsed = heapMemoryUsage.getUsed();
            long heapMax = heapMemoryUsage.getMax();
            long heapCommitted = heapMemoryUsage.getCommitted();

            double heapUsagePercent = heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0;

            // Redis健康检查
            boolean redisHealthy = false;
            try {
                String result = stringRedisTemplate.execute((RedisCallback<String>) connection -> {
                    return connection.ping();
                });
                redisHealthy = "PONG".equals(result);
            } catch (Exception e) {
                logger.warn("Redis健康检查失败", e);
            }

            // 数据库健康检查
            boolean dbHealthy = false;
            try {
                userRepository.count();
                dbHealthy = true;
            } catch (Exception e) {
                logger.warn("数据库健康检查失败", e);
            }

            health.put("success", true);
            health.put("status", (redisHealthy && dbHealthy && heapUsagePercent < 90) ? "HEALTHY" : "WARNING");
            health.put("heapUsedMB", heapUsed / 1024 / 1024);
            health.put("heapMaxMB", heapMax / 1024 / 1024);
            health.put("heapUsagePercent", String.format("%.2f", heapUsagePercent));
            health.put("redisHealthy", redisHealthy);
            health.put("databaseHealthy", dbHealthy);
            health.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            logger.error("获取系统健康状态失败", e);
            health.put("success", false);
            health.put("message", "获取系统健康状态失败");
        }

        return health;
    }
}
