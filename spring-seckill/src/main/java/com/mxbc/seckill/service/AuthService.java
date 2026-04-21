package com.mxbc.seckill.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.UserRepository;
import com.mxbc.seckill.util.SM3Util;
import com.mxbc.seckill.util.PasswordUtil;

@Service
public class AuthService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_EXPIRE_SECONDS = 600;
    
    public LoginResult login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return LoginResult.failure("用户不存在");
        }
        
        if (user.getStatus() == null || user.getStatus() != 1) {
            return LoginResult.failure("账号已被禁用");
        }
        
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            logger.warn("用户密码盐值为空，可能是旧数据: username={}", username);
            if (!password.equals(user.getPassword())) {
                return LoginResult.failure("密码错误");
            }
        } else {
            if (!SM3Util.verifyPassword(password, user.getPassword(), user.getSalt())) {
                return LoginResult.failure("密码错误");
            }
        }
        
        String token = generateToken();
        String sessionKey = SESSION_PREFIX + token;
        
        try {
            stringRedisTemplate.opsForValue().set(sessionKey, String.valueOf(user.getId()));
            stringRedisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(SESSION_EXPIRE_SECONDS));
            logger.info("用户登录成功: username={}, token={}, userId={}", username, token, user.getId());
        } catch (Exception e) {
            logger.error("存储着session到Redis失败", e);
            return LoginResult.failure("登录失败：系统错误");
        }
        
        return LoginResult.success(token, user);
    }
    
    public Long getUserIdByToken(String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("Token为空");
            return null;
        }
        
        String sessionKey = SESSION_PREFIX + token;
        
        try {
            String userIdStr = stringRedisTemplate.opsForValue().get(sessionKey);
            
            if (userIdStr == null || userIdStr.isEmpty()) {
                logger.warn("Redis中未找到token对应的session: token={}", token);
                return null;
            }
            
            logger.info("从Redis获取到userId: token={}, userId={}", token, userIdStr);
            
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            logger.error("解析用户ID失败: token={}", token, e);
            return null;
        } catch (Exception e) {
            logger.error("从Redis获取session失败: token={}", token, e);
            return null;
        }
    }
    
    public User getUserByToken(String token) {
        // 处理带有"Bearer "前缀的token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = getUserIdByToken(token);
        if (userId == null) {
            return null;
        }
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("未找到用户: userId={}", userId);
            }
            return user;
        } catch (Exception e) {
            logger.error("查询用户失败: userId={}", userId, e);
            return null;
        }
    }
    
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String sessionKey = SESSION_PREFIX + token;
        stringRedisTemplate.delete(sessionKey);
        
        logger.info("用户登出: token={}", token);
    }
    
    public boolean isTokenValid(String token) {
        return getUserIdByToken(token) != null;
    }
    
    public void register(String username, String password, String email, String phone, String nickname) {
        User existingUser = userRepository.findByUsername(username).orElse(null);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (!isPasswordStrong(password)) {
            throw new RuntimeException("密码强度不足：密码长度至少12位，需同时包含大小写字母、数字和特殊符号");
        }
        
        String salt = SM3Util.generateSalt();
        String hashedPassword = SM3Util.hashWithSalt(password, salt);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setSalt(salt);
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setAge(18);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(1);
        
        userRepository.save(user);
        
        logger.info("用户注册成功: username={}", username);
    }
    
    private boolean isPasswordStrong(String password) {
        return PasswordUtil.isPasswordStrong(password);
    }
    
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    public static class LoginResult {
        private boolean success;
        private String message;
        private String token;
        private User user;
        
        public static LoginResult success(String token, User user) {
            LoginResult result = new LoginResult();
            result.success = true;
            result.message = "登录成功";
            result.token = token;
            result.user = user;
            return result;
        }
        
        public static LoginResult failure(String message) {
            LoginResult result = new LoginResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getToken() {
            return token;
        }
        
        public User getUser() {
            return user;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public void setUser(User user) {
            this.user = user;
        }
    }
}
