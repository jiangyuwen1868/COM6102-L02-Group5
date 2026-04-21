package com.mxbc.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.service.AuthService;
import com.mxbc.seckill.service.CaptchaService;
import com.mxbc.seckill.util.CaptchaUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, Object>> generateCaptcha() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            CaptchaUtil.CaptchaResult captcha = CaptchaUtil.generateCaptcha();
            captchaService.saveCaptcha(captcha.getCaptchaId(), captcha.getCode());
            
            result.put("code", 0);
            result.put("message", "验证码生成成功");
            result.put("captchaId", captcha.getCaptchaId());
            result.put("image", "data:image/png;base64," + captcha.getImageBase64());
        } catch (Exception e) {
            logger.error("生成验证码失败", e);
            result.put("code", -1);
            result.put("message", "生成验证码失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (request.getCaptchaId() == null || request.getCaptchaId().isEmpty() ||
                request.getCaptchaCode() == null || request.getCaptchaCode().isEmpty()) {
                result.put("code", -1);
                result.put("message", "请输入验证码");
                return ResponseEntity.ok(result);
            }
            
            if (!captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
                result.put("code", -1);
                result.put("message", "验证码错误");
                return ResponseEntity.ok(result);
            }
            
            AuthService.LoginResult loginResult = authService.login(request.getUsername(), request.getPassword());
            
            if (loginResult.isSuccess()) {
                result.put("code", 0);
                result.put("message", "登录成功");
                result.put("token", loginResult.getToken());
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", loginResult.getUser().getId());
                userInfo.put("username", loginResult.getUser().getUsername());
                userInfo.put("nickname", loginResult.getUser().getNickname());
                userInfo.put("email", loginResult.getUser().getEmail());
                result.put("user", userInfo);
            } else {
                result.put("code", -1);
                result.put("message", loginResult.getMessage());
            }
        } catch (Exception e) {
            logger.error("登录失败", e);
            result.put("code", -1);
            result.put("message", "登录失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPhone(),
                request.getNickname()
            );
            
            result.put("code", 0);
            result.put("message", "注册成功");
        } catch (Exception e) {
            logger.error("注册失败", e);
            result.put("code", -1);
            result.put("message", "注册失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            authService.logout(token);
            result.put("code", 0);
            result.put("message", "登出成功");
        } catch (Exception e) {
            logger.error("登出失败", e);
            result.put("code", -1);
            result.put("message", "登出失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = authService.getUserByToken(token);
            
            if (user != null) {
                result.put("code", 0);
                result.put("message", "登录有效");
                result.put("authenticated", true);
                result.put("user", user);
            } else {
                result.put("code", -1);
                result.put("message", "未登录或登录已过期");
                result.put("authenticated", false);
            }
        } catch (Exception e) {
            logger.error("检查登录状态失败", e);
            result.put("code", -1);
            result.put("message", "检查失败：" + e.getMessage());
            result.put("authenticated", false);
        }
        
        return ResponseEntity.ok(result);
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        private String captchaId;
        private String captchaCode;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
        
        public void setCaptchaId(String captchaId) {
            this.captchaId = captchaId;
        }
        
        public String getCaptchaCode() {
            return captchaCode;
        }
        
        public void setCaptchaCode(String captchaCode) {
            this.captchaCode = captchaCode;
        }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String nickname;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}