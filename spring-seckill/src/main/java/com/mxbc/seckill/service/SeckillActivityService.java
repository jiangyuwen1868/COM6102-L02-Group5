package com.mxbc.seckill.service;

import com.mxbc.seckill.common.StockPreloader;
import com.mxbc.seckill.controller.WebSocketController;
import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.SeckillActivity.ActivityStatus;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.SeckillActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeckillActivityService {

    private static final Logger logger = LoggerFactory.getLogger(SeckillActivityService.class);

    @Autowired
    private SeckillActivityRepository activityRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockPreloader stockPreloader;

    @Autowired
    private WebSocketController webSocketController;

    public List<SeckillActivity> getAllActivities() {
        updateActivityStatuses();
        List<SeckillActivity> activities = activityRepository.findAll();
        // 确保活动库存与商品库存一致
        for (SeckillActivity activity : activities) {
            if (activity.getProduct() != null) {
                activity.setStock(activity.getProduct().getStock());
            }
        }
        return activities;
    }

    public List<SeckillActivity> getActiveActivities() {
        updateActivityStatuses();
        List<SeckillActivity> activities = activityRepository.findByStatusOrderByStartTimeAsc(ActivityStatus.ACTIVE);
        // 确保活动库存与商品库存一致
        for (SeckillActivity activity : activities) {
            if (activity.getProduct() != null) {
                activity.setStock(activity.getProduct().getStock());
            }
        }
        return activities;
    }

    public List<SeckillActivity> getUpcomingActivities() {
        updateActivityStatuses();
        List<SeckillActivity> activities = activityRepository.findByStatusOrderByStartTimeAsc(ActivityStatus.UPCOMING);
        // 确保活动库存与商品库存一致
        for (SeckillActivity activity : activities) {
            if (activity.getProduct() != null) {
                activity.setStock(activity.getProduct().getStock());
            }
        }
        return activities;
    }

    public SeckillActivity getActivityById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    @Transactional
    public Map<String, Object> createActivity(SeckillActivity activity) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证商品是否存在
            Product product = productRepository.findById(activity.getProduct().getId()).orElse(null);
            if (product == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }

            // 验证活动时间
            if (activity.getStartTime().isAfter(activity.getEndTime())) {
                result.put("success", false);
                result.put("message", "开始时间不能晚于结束时间");
                return result;
            }

            // 验证库存
            if (activity.getStock() <= 0 || activity.getStock() > product.getStock()) {
                result.put("success", false);
                result.put("message", "秒杀库存设置不合理");
                return result;
            }

            // 设置初始状态
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(activity.getStartTime())) {
                activity.setStatus(ActivityStatus.UPCOMING);
            } else if (now.isAfter(activity.getEndTime())) {
                result.put("success", false);
                result.put("message", "活动结束时间不能早于当前时间");
                return result;
            } else {
                activity.setStatus(ActivityStatus.ACTIVE);
            }

            activityRepository.save(activity);

            // 预热库存
            stockPreloader.updateStockInRedis(product.getId(), activity.getStock());

            result.put("success", true);
            result.put("message", "活动创建成功");
            result.put("activityId", activity.getId());

            logger.info("创建秒杀活动: {}, 商品: {}, 库存: {}",
                    activity.getName(), product.getId(), activity.getStock());

            return result;
        } catch (Exception e) {
            logger.error("创建秒杀活动失败", e);
            result.put("success", false);
            result.put("message", "创建活动失败");
            return result;
        }
    }

    @Transactional
    public Map<String, Object> updateActivity(Long id, SeckillActivity activity) {
        Map<String, Object> result = new HashMap<>();

        try {
            SeckillActivity existingActivity = activityRepository.findById(id).orElse(null);
            if (existingActivity == null) {
                result.put("success", false);
                result.put("message", "活动不存在");
                return result;
            }

            if (existingActivity.getStatus() == ActivityStatus.ENDED) {
                result.put("success", false);
                result.put("message", "已结束的活动不能修改");
                return result;
            }

            existingActivity.setName(activity.getName());
            existingActivity.setDescription(activity.getDescription());
            existingActivity.setSeckillPrice(activity.getSeckillPrice());
            existingActivity.setStartTime(activity.getStartTime());
            existingActivity.setEndTime(activity.getEndTime());

            activityRepository.save(existingActivity);

            result.put("success", true);
            result.put("message", "活动更新成功");

            logger.info("更新秒杀活动: {}", id);

            return result;
        } catch (Exception e) {
            logger.error("更新秒杀活动失败", e);
            result.put("success", false);
            result.put("message", "更新活动失败");
            return result;
        }
    }

    @Transactional
    public Map<String, Object> cancelActivity(Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            SeckillActivity activity = activityRepository.findById(id).orElse(null);
            if (activity == null) {
                result.put("success", false);
                result.put("message", "活动不存在");
                return result;
            }

            if (activity.getStatus() == ActivityStatus.ENDED) {
                result.put("success", false);
                result.put("message", "已结束的活动不能取消");
                return result;
            }

            activity.setStatus(ActivityStatus.CANCELLED);
            activityRepository.save(activity);

            result.put("success", true);
            result.put("message", "活动已取消");

            logger.info("取消秒杀活动: {}", id);

            return result;
        } catch (Exception e) {
            logger.error("取消秒杀活动失败", e);
            result.put("success", false);
            result.put("message", "取消活动失败");
            return result;
        }
    }

    @Scheduled(fixedRate = 60000) // 每分钟更新一次活动状态
    public void updateActivityStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<SeckillActivity> activities = activityRepository.findAll();
        for (SeckillActivity activity : activities) {
            ActivityStatus oldStatus = activity.getStatus();
            ActivityStatus newStatus = oldStatus;

            if (activity.getStatus() == ActivityStatus.CANCELLED) {
                continue;
            }

            if (now.isBefore(activity.getStartTime())) {
                newStatus = ActivityStatus.UPCOMING;
            } else if (now.isAfter(activity.getEndTime())) {
                newStatus = ActivityStatus.ENDED;
            } else {
                newStatus = ActivityStatus.ACTIVE;
            }

            if (oldStatus != newStatus) {
                activity.setStatus(newStatus);
                activityRepository.save(activity);
                logger.info("活动 {} 状态更新: {} -> {}", activity.getId(), oldStatus, newStatus);

                // 发送状态变更通知
                webSocketController.sendSystemMessage(
                        "秒杀活动 " + activity.getName() + " 状态已更新为 " + newStatus.getDescription());
            }
        }
    }

    public Map<String, Object> getActivityStats(Long activityId) {
        Map<String, Object> stats = new HashMap<>();

        SeckillActivity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            stats.put("success", false);
            stats.put("message", "活动不存在");
            return stats;
        }

        stats.put("success", true);
        stats.put("activityId", activityId);
        stats.put("activityName", activity.getName());
        stats.put("status", activity.getStatus().name());
        stats.put("totalStock", activity.getStock());
        stats.put("seckillPrice", activity.getSeckillPrice());
        stats.put("startTime", activity.getStartTime());
        stats.put("endTime", activity.getEndTime());
        stats.put("createdAt", activity.getCreatedAt());
        // 添加缺失的统计字段
        stats.put("soldCount", 0); // 暂时设置为0，实际应该从订单表中统计
        stats.put("remainingStock", activity.getStock()); // 暂时设置为总库存，实际应该是总库存减去已售数量
        stats.put("participantCount", 0); // 暂时设置为0，实际应该从订单表中统计

        return stats;
    }
}
