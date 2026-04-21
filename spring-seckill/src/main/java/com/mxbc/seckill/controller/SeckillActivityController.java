package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.SeckillActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class SeckillActivityController {

    private static final Logger logger = LoggerFactory.getLogger(SeckillActivityController.class);

    @Autowired
    private SeckillActivityService activityService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<?> getAllActivities() {
        try {
            List<SeckillActivity> activities = activityService.getAllActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            logger.error("获取活动列表失败", e);
            return ResponseEntity.status(500).body("获取活动列表失败");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveActivities() {
        try {
            List<SeckillActivity> activities = activityService.getActiveActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            logger.error("获取进行中的活动失败", e);
            return ResponseEntity.status(500).body("获取活动失败");
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingActivities() {
        try {
            List<SeckillActivity> activities = activityService.getUpcomingActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            logger.error("获取即将开始的活动失败", e);
            return ResponseEntity.status(500).body("获取活动失败");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        try {
            SeckillActivity activity = activityService.getActivityById(id);
            if (activity == null) {
                return ResponseEntity.status(404).body("活动不存在");
            }
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            logger.error("获取活动详情失败", e);
            return ResponseEntity.status(500).body("获取活动详情失败");
        }
    }

    @PostMapping
    public ResponseEntity<?> createActivity(@RequestBody Map<String, Object> activityData, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        // 这里应该检查用户是否有管理员权限

        try {
            SeckillActivity activity = new SeckillActivity();
            activity.setName((String) activityData.get("name"));
            activity.setDescription((String) activityData.get("description"));
            
            // 处理商品ID
            if (activityData.get("productId") != null) {
                Product product = new Product();
                product.setId(Long.parseLong(activityData.get("productId").toString()));
                activity.setProduct(product);
            }
            
            // 处理价格字段
            if (activityData.get("seckillPrice") != null) {
                if (activityData.get("seckillPrice") instanceof String) {
                    activity.setSeckillPrice(new BigDecimal((String) activityData.get("seckillPrice")));
                } else if (activityData.get("seckillPrice") instanceof Number) {
                    activity.setSeckillPrice(new BigDecimal(activityData.get("seckillPrice").toString()));
                }
            }
            
            activity.setStock((Integer) activityData.get("stock"));
            
            // 处理时间字段
            if (activityData.get("startTime") != null) {
                try {
                    String startTimeStr = activityData.get("startTime").toString();
                    if (startTimeStr.contains("T")) {
                        startTimeStr = startTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    activity.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
                } catch (Exception e) {
                    logger.error("解析开始时间失败", e);
                }
            }
            
            if (activityData.get("endTime") != null) {
                try {
                    String endTimeStr = activityData.get("endTime").toString();
                    if (endTimeStr.contains("T")) {
                        endTimeStr = endTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    activity.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
                } catch (Exception e) {
                    logger.error("解析结束时间失败", e);
                }
            }
            
            Map<String, Object> result = activityService.createActivity(activity);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("创建活动失败", e);
            return ResponseEntity.status(500).body("创建活动失败");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> activityData,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            SeckillActivity activity = new SeckillActivity();
            activity.setName((String) activityData.get("name"));
            activity.setDescription((String) activityData.get("description"));
            
            // 处理商品ID
            if (activityData.get("productId") != null) {
                Product product = new Product();
                product.setId(Long.parseLong(activityData.get("productId").toString()));
                activity.setProduct(product);
            }
            
            // 处理价格字段
            if (activityData.get("seckillPrice") != null) {
                if (activityData.get("seckillPrice") instanceof String) {
                    activity.setSeckillPrice(new BigDecimal((String) activityData.get("seckillPrice")));
                } else if (activityData.get("seckillPrice") instanceof Number) {
                    activity.setSeckillPrice(new BigDecimal(activityData.get("seckillPrice").toString()));
                }
            }
            
            activity.setStock((Integer) activityData.get("stock"));
            
            // 处理时间字段
            if (activityData.get("startTime") != null) {
                try {
                    String startTimeStr = activityData.get("startTime").toString();
                    if (startTimeStr.contains("T")) {
                        startTimeStr = startTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    activity.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
                } catch (Exception e) {
                    logger.error("解析开始时间失败", e);
                }
            }
            
            if (activityData.get("endTime") != null) {
                try {
                    String endTimeStr = activityData.get("endTime").toString();
                    if (endTimeStr.contains("T")) {
                        endTimeStr = endTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    activity.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
                } catch (Exception e) {
                    logger.error("解析结束时间失败", e);
                }
            }
            
            Map<String, Object> result = activityService.updateActivity(id, activity);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("更新活动失败", e);
            return ResponseEntity.status(500).body("更新活动失败");
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelActivity(@PathVariable Long id, HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> result = activityService.cancelActivity(id);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("取消活动失败", e);
            return ResponseEntity.status(500).body("取消活动失败");
        }
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getActivityStats(@PathVariable Long id) {
        try {
            Map<String, Object> result = activityService.getActivityStats(id);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            logger.error("获取活动统计失败", e);
            return ResponseEntity.status(500).body("获取活动统计失败");
        }
    }
}
