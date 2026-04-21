package com.mxbc.seckill.service;

import com.mxbc.seckill.entity.Order;
import com.mxbc.seckill.entity.OrderStatus;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.OrderRepository;
import com.mxbc.seckill.entity.repository.UserRepository;
import com.mxbc.seckill.util.PasswordUtil;
import com.mxbc.seckill.util.SM3Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    public User getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user;
    }

    @Transactional
    public Map<String, Object> updateUserProfile(Long userId, Map<String, String> profileData) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (profileData.containsKey("email")) {
            user.setEmail(profileData.get("email"));
        }
        if (profileData.containsKey("phone")) {
            user.setPhone(profileData.get("phone"));
        }

        userRepository.save(user);

        result.put("success", true);
        result.put("message", "用户信息更新成功");

        logger.info("用户 {} 更新了个人信息", userId);

        return result;
    }

    @Transactional
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 验证旧密码
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            if (!oldPassword.equals(user.getPassword())) {
                result.put("success", false);
                result.put("message", "旧密码错误");
                return result;
            }
        } else {
            if (!SM3Util.verifyPassword(oldPassword, user.getPassword(), user.getSalt())) {
                result.put("success", false);
                result.put("message", "旧密码错误");
                return result;
            }
        }

        // 验证新密码强度
        if (newPassword.length() < 6) {
            result.put("success", false);
            result.put("message", "新密码长度不能少于6位");
            return result;
        }

        // 更新密码
        String salt = SM3Util.generateSalt();
        String hashedPassword = SM3Util.hashWithSalt(newPassword, salt);
        user.setPassword(hashedPassword);
        user.setSalt(salt);
        userRepository.save(user);

        result.put("success", true);
        result.put("message", "密码修改成功");

        logger.info("用户 {} 修改了密码", userId);

        return result;
    }

    public Map<String, Object> getSeckillHistory(Long userId) {
        Map<String, Object> result = new HashMap<>();

        List<Order> orders = orderRepository.findByUserId(userId);

        int successCount = 0;
        int failedCount = 0;
        double totalSpent = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PAID || 
                order.getStatus() == OrderStatus.SHIPPED || 
                order.getStatus() == OrderStatus.DELIVERED) {
                successCount++;
                totalSpent += order.getTotalAmount().doubleValue();
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                failedCount++;
            }
        }

        result.put("success", true);
        result.put("totalOrders", orders.size());
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("totalSpent", totalSpent);
        result.put("orders", orders);

        return result;
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            stats.put("success", false);
            stats.put("message", "用户不存在");
            return stats;
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        long pendingCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long paidCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long shippedCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long deliveredCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        double totalSpent = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || 
                           o.getStatus() == OrderStatus.SHIPPED || 
                           o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();

        stats.put("success", true);
        stats.put("username", user.getUsername());
        stats.put("memberSince", user.getCreateTime());
        stats.put("totalOrders", orders.size());
        stats.put("pendingOrders", pendingCount);
        stats.put("paidOrders", paidCount);
        stats.put("shippedOrders", shippedCount);
        stats.put("deliveredOrders", deliveredCount);
        stats.put("cancelledOrders", cancelledCount);
        stats.put("totalSpent", totalSpent);

        return stats;
    }
}
