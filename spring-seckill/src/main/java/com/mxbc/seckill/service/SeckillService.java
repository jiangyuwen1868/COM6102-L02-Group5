package com.mxbc.seckill.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mxbc.seckill.common.SeckillLuaScript;
import com.mxbc.seckill.common.StockPreloader;
import com.mxbc.seckill.common.TrafficShapingService;
import com.mxbc.seckill.controller.WebSocketController;
import com.mxbc.seckill.entity.OrderMessage;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.SeckillActivityRepository;
import com.mxbc.seckill.vo.SeckillResult;

@Service
public class SeckillService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillService.class);
    
    @Autowired
    private SeckillLuaScript seckillLuaScript;
    
    @Autowired
    private TrafficShapingService trafficShapingService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private AsyncOrderService asyncOrderService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SeckillActivityRepository seckillActivityRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private WebSocketController webSocketController;
    
    @Autowired
    private StockPreloader stockPreloader;
    
    @Transactional
    public SeckillResult seckill(Long productId, Long userId) {
        logger.info("开始执行秒杀操作: 商品ID={}, 用户ID={}", productId, userId);
        
        // 1. 检查商品是否存在
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            logger.warn("商品不存在: 商品ID={}", productId);
            return SeckillResult.failure("商品不存在");
        }
        
        logger.info("商品存在: 商品ID={}, 名称={}, 库存={}", productId, product.getName(), product.getStock());
        
        // 2. 检查秒杀活动状态（基于秒杀活动时间）
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> activities = seckillActivityRepository.findByProductId(productId);
        if (activities.isEmpty()) {
            logger.warn("秒杀活动不存在: 商品ID={}", productId);
            return SeckillResult.failure("秒杀活动不存在");
        }
        
        logger.info("秒杀活动存在: 商品ID={}, 活动数量={}", productId, activities.size());
        
        SeckillActivity activity = activities.stream()
                .filter(a -> a.getStatus() == SeckillActivity.ActivityStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        
        if (activity == null) {
            logger.warn("秒杀活动未开始或已结束: 商品ID={}", productId);
            return SeckillResult.failure("秒杀活动未开始或已结束");
        }
        
        logger.info("秒杀活动状态正常: 活动ID={}, 商品ID={}, 库存={}", activity.getId(), productId, activity.getStock());
        
        // 3. 检查Redis中是否有库存数据，如果没有，预热库存
        if (stockPreloader.getStockFromRedis(productId) == null) {
            logger.info("Redis中没有商品 {} 的库存数据，预热库存", productId);
            stockPreloader.preloadProductStock(productId);
        }
        
        // 4. 流量控制
        if (!trafficShapingService.tryAcquire("seckill:" + productId, 1)) {
            return SeckillResult.failure("当前参与人数过多，请稍后再试");
        }
        
        // 5. 执行秒杀（原子性操作）
        logger.info("开始执行秒杀操作: 商品ID={}, 用户ID={}", productId, userId);
        SeckillResult result = seckillLuaScript.executeSeckill(productId, userId);
        
        logger.info("秒杀执行结果: 商品ID={}, 用户ID={}, 结果={}", productId, userId, result);
        
        // 根据错误码设置中文消息
        if (result.getCode() == 1) {
            result.setMessage("库存不存在");
        } else if (result.getCode() == 2) {
            result.setMessage("库存不足");
        } else if (result.getCode() == 3) {
            result.setMessage("您已参与过本次秒杀，每人限购一件");
        } else if (result.getCode() == 0) {
            result.setMessage("秒杀成功");
        }
        
        if (result.isSuccess()) {
            // 更新数据库库存，与Redis保持一致
            product.setStock(result.getStock());
            productRepository.save(product);
            
            // 更新秒杀活动的库存
            updateSeckillActivityStock(productId, result.getStock());
            
            OrderMessage orderMessage = new OrderMessage(productId, userId, result.getOrderId());
            asyncOrderService.createOrderAsync(orderMessage);
            
            // 发送WebSocket通知
            webSocketController.sendStockUpdate(productId, result.getStock());
            webSocketController.sendSeckillResult(userId, true, result.getMessage());
        } else {
            // 发送失败通知
            webSocketController.sendSeckillResult(userId, false, result.getMessage());
        }
        
        return result;
    }
    
    /**
     * 更新秒杀活动的库存
     */
    private void updateSeckillActivityStock(Long productId, Integer stock) {
        try {
            // 查找关联的秒杀活动
            List<SeckillActivity> activities = seckillActivityRepository.findByProductId(productId);
            for (SeckillActivity activity : activities) {
                activity.setStock(stock);
                seckillActivityRepository.save(activity);
                logger.info("更新秒杀活动库存: 活动ID={}, 商品ID={}, 新库存={}", 
                        activity.getId(), productId, stock);
            }
        } catch (Exception e) {
            logger.error("更新秒杀活动库存失败", e);
        }
    }
}