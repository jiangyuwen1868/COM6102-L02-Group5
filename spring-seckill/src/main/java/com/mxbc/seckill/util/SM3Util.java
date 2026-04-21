package com.mxbc.seckill.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class SM3Util {
    
    private static final SecureRandom random = new SecureRandom();
    
    public static String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Hex.toHexString(salt);
    }
    
    public static String hash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("输入不能为空");
        }
        
        SM3Digest digest = new SM3Digest();
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        digest.update(inputBytes, 0, inputBytes.length);
        
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        
        return Hex.toHexString(result);
    }
    
    public static String hashWithSalt(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("密码和盐值不能为空");
        }
        
        String saltedPassword = password + salt;
        return hash(saltedPassword);
    }
    
    public static boolean verifyPassword(String inputPassword, String storedHash, String salt) {
        if (inputPassword == null || storedHash == null || salt == null) {
            return false;
        }
        
        String computedHash = hashWithSalt(inputPassword, salt);
        return computedHash.equalsIgnoreCase(storedHash);
    }
}
