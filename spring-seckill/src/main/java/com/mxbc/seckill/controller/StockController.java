package com.mxbc.seckill.controller;

import com.mxbc.seckill.common.StockPreloader;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.StockLog;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.StockLogRepository;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.StockRollbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockRollbackService stockRollbackService;

    @Autowired
    private StockPreloader stockPreloader;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockLogRepository stockLogRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/products")
    public ResponseEntity<?> getAllProductsStock(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            List<Product> products = productRepository.findAll();
            List<Map<String, Object>> stockInfoList = new ArrayList<>();

            for (Product product : products) {
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("productId", product.getId());
                stockInfo.put("productName", product.getName());
                stockInfo.put("price", product.getPrice());
                stockInfo.put("dbStock", product.getStock());
                
                // 获取Redis中的库存
                String redisStockKey = "stock:" + product.getId();
                String redisStockStr = stringRedisTemplate.opsForValue().get(redisStockKey);
                Integer redisStock = redisStockStr != null ? Integer.parseInt(redisStockStr) : null;
                stockInfo.put("redisStock", redisStock);
                
                stockInfoList.add(stockInfo);
                logger.info("商品 {} 的价格: {}", product.getName(), product.getPrice());
            }

            return ResponseEntity.ok(stockInfoList);
        } catch (Exception e) {
            logger.error("获取库存信息失败", e);
            return ResponseEntity.status(500).body("获取库存信息失败");
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductStock(
            @PathVariable Long productId,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(404).body("商品不存在");
            }

            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("productId", product.getId());
            stockInfo.put("productName", product.getName());
            stockInfo.put("price", product.getPrice());
            stockInfo.put("dbStock", product.getStock());
            
            // 获取Redis中的库存
            String redisStockKey = "stock:" + product.getId();
            String redisStockStr = stringRedisTemplate.opsForValue().get(redisStockKey);
            Integer redisStock = redisStockStr != null ? Integer.parseInt(redisStockStr) : null;
            stockInfo.put("redisStock", redisStock);

            return ResponseEntity.ok(stockInfo);
        } catch (Exception e) {
            logger.error("获取商品库存信息失败", e);
            return ResponseEntity.status(500).body("获取库存信息失败");
        }
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<?> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer newStock,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(404).body("商品不存在");
            }

            if (newStock < 0) {
                return ResponseEntity.badRequest().body("库存数量不能为负数");
            }

            int oldStock = product.getStock();
            product.setStock(newStock);
            productRepository.save(product);

            // 更新Redis库存
            stockPreloader.updateStockInRedis(productId, newStock);

            // 记录库存变更日志
            StockLog stockLog = new StockLog();
            stockLog.setProduct(product);
            stockLog.setBeforeStock(oldStock);
            stockLog.setAfterStock(newStock);
            stockLog.setChangeAmount(newStock - oldStock);
            stockLog.setOperationType(StockLog.StockOperationType.MANUAL);
            stockLog.setOperationDesc(reason != null ? reason : "手动调整库存");
            stockLog.setRedisStock(newStock);
            stockLogRepository.save(stockLog);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "库存调整成功");
            result.put("oldStock", oldStock);
            result.put("newStock", newStock);

            logger.info("操作员 {} 调整商品 {} 库存: {} -> {}", user.getId(), productId, oldStock, newStock);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("调整库存失败", e);
            return ResponseEntity.status(500).body("调整库存失败");
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getStockLogs(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            List<StockLog> logs;
            if (productId != null) {
                logs = stockLogRepository.findByProduct_IdOrderByCreatedAtDesc(productId);
            } else {
                logs = stockLogRepository.findAll();
                // 按创建时间倒序排序
                logs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }

            // 限制返回数量
            if (logs.size() > limit) {
                logs = logs.subList(0, limit);
            }

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("获取库存日志失败", e);
            return ResponseEntity.status(500).body("获取库存日志失败");
        }
    }

    @PostMapping("/rollback/{orderId}")
    public ResponseEntity<?> manualRollbackStock(
            @PathVariable String orderId,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        // 这里应该检查用户是否有管理员权限
        // 简化处理，假设所有登录用户都可以操作

        try {
            boolean success = stockRollbackService.manualRollbackStock(orderId, user.getId());
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("success", true);
                result.put("message", "库存回滚成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "库存回滚失败，可能订单状态不允许");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("手动回滚库存失败", e);
            return ResponseEntity.status(500).body("操作失败");
        }
    }

    @PostMapping("/sync/{productId}")
    public ResponseEntity<?> syncStockToRedis(
            @PathVariable Long productId,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            stockRollbackService.syncStockToRedis(productId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "库存同步成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("同步库存失败", e);
            return ResponseEntity.status(500).body("同步失败");
        }
    }

    @PostMapping("/sync-all")
    public ResponseEntity<?> syncAllStockToRedis(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            stockRollbackService.syncAllStockToRedis();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有库存同步成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("同步所有库存失败", e);
            return ResponseEntity.status(500).body("同步失败");
        }
    }

    @PostMapping("/preload/{productId}")
    public ResponseEntity<?> preloadProductStock(
            @PathVariable Long productId,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            stockPreloader.preloadProductStock(productId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "库存预热成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("预热库存失败", e);
            return ResponseEntity.status(500).body("预热失败");
        }
    }
}
