package com.mxbc.seckill.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.Base64;
import java.util.UUID;

public class CaptchaUtil {
    
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 5;
    private static final int LINE_COUNT = 5;
    private static final int NOISE_COUNT = 30;
    
    private static final Random random = new Random();
    
    public static class CaptchaResult {
        private String code;
        private String imageBase64;
        private String captchaId;
        
        public CaptchaResult(String code, String imageBase64, String captchaId) {
            this.code = code;
            this.imageBase64 = imageBase64;
            this.captchaId = captchaId;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getImageBase64() {
            return imageBase64;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
    }
    
    public static CaptchaResult generateCaptcha() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);
        
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            String charStr = CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())) + "";
            code.append(charStr);
            
            g.setColor(getRandomColor(50, 150));
            g.drawString(charStr, 20 * i + 10, 30);
        }
        
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(getRandomColor(100, 200));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                     random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        for (int i = 0; i < NOISE_COUNT; i++) {
            g.setColor(getRandomColor(150, 250));
            g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
        
        g.dispose();
        
        String imageBase64 = imageToBase64(image);
        String captchaId = UUID.randomUUID().toString();
        
        return new CaptchaResult(code.toString(), imageBase64, captchaId);
    }
    
    private static Color getRandomColor(int min, int max) {
        int r = random.nextInt(max - min) + min;
        int g = random.nextInt(max - min) + min;
        int b = random.nextInt(max - min) + min;
        return new Color(r, g, b);
    }
    
    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
}
