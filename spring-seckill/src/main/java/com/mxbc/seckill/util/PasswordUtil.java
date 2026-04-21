package com.mxbc.seckill.util;

public class PasswordUtil {
    
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 12) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            
            if (c >= 'A' && c <= 'Z') {
                hasUpperCase = true;
            } else if (c >= 'a' && c <= 'z') {
                hasLowerCase = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecial;
    }
    
    public static String getPasswordStrengthInfo(String password) {
        if (password == null || password.length() < 12) {
            return "密码长度不足12位";
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            
            if (c >= 'A' && c <= 'Z') {
                hasUpperCase = true;
            } else if (c >= 'a' && c <= 'z') {
                hasLowerCase = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        
        if (hasUpperCase && hasLowerCase && hasDigit && hasSpecial) {
            return "密码强度：强";
        }
        
        StringBuilder missing = new StringBuilder();
        if (!hasUpperCase) missing.append("大写字母");
        if (!hasLowerCase) {
            if (missing.length() > 0) missing.append("、");
            missing.append("小写字母");
        }
        if (!hasDigit) {
            if (missing.length() > 0) missing.append("、");
            missing.append("数字");
        }
        if (!hasSpecial) {
            if (missing.length() > 0) missing.append("、");
            missing.append("特殊符号");
        }
        
        return "密码强度：弱（还需要：" + missing.toString() + "）";
    }
}
