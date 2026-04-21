package com.mxbc.seckill.controller;

import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            User profile = userService.getUserProfile(user.getId());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.status(500).body("获取用户信息失败");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody Map<String, String> profileData,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> result = userService.updateUserProfile(user.getId(), profileData);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ResponseEntity.status(500).body("更新用户信息失败");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> passwordData,
            HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("请提供旧密码和新密码");
        }

        try {
            Map<String, Object> result = userService.changePassword(user.getId(), oldPassword, newPassword);
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("修改密码失败", e);
            return ResponseEntity.status(500).body("修改密码失败");
        }
    }

    @GetMapping("/seckill-history")
    public ResponseEntity<?> getSeckillHistory(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> result = userService.getSeckillHistory(user.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取秒杀历史失败", e);
            return ResponseEntity.status(500).body("获取秒杀历史失败");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }

        try {
            Map<String, Object> stats = userService.getUserStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取用户统计失败", e);
            return ResponseEntity.status(500).body("获取用户统计失败");
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        User user = authService.getUserByToken(request.getHeader("Authorization"));
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", -1);
            result.put("message", "请先登录");
            result.put("authenticated", false);
            return ResponseEntity.ok(result);
        }

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("message", "获取用户信息成功");
            result.put("authenticated", true);
            result.put("user", user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", -1);
            result.put("message", "获取用户信息失败");
            result.put("authenticated", false);
            return ResponseEntity.ok(result);
        }
    }
}
