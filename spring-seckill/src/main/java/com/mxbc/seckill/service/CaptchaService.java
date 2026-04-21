package com.mxbc.seckill.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CaptchaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_SECONDS = 300;
    
    public void saveCaptcha(String captchaId, String code) {
        String key = CAPTCHA_PREFIX + captchaId;
        try {
            stringRedisTemplate.opsForValue().set(key, code.toLowerCase());
            stringRedisTemplate.expire(key, Duration.ofSeconds(CAPTCHA_EXPIRE_SECONDS));
            logger.info("验证码已保存: captchaId={}, code={}", captchaId, code);
        } catch (Exception e) {
            logger.error("保存验证码失败", e);
            throw new RuntimeException("保存验证码失败", e);
        }
    }
    
    public boolean validateCaptcha(String captchaId, String inputCode) {
        if (captchaId == null || captchaId.isEmpty() || inputCode == null || inputCode.isEmpty()) {
            logger.warn("验证码参数为空");
            return false;
        }
        
        String key = CAPTCHA_PREFIX + captchaId;
        try {
            String storedCode = stringRedisTemplate.opsForValue().get(key);
            
            if (storedCode == null) {
                logger.warn("验证码不存在或已过期: captchaId={}", captchaId);
                return false;
            }
            
            boolean isValid = storedCode.equalsIgnoreCase(inputCode);
            
            if (isValid) {
                stringRedisTemplate.delete(key);
                logger.info("验证码验证成功: captchaId={}", captchaId);
            } else {
                logger.warn("验证码验证失败: captchaId={}, inputCode={}, storedCode={}", 
                    captchaId, inputCode, storedCode);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("验证码验证异常", e);
            return false;
        }
    }
}
