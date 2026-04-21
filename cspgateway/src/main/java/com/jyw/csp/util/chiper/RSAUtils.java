package com.jyw.csp.util.chiper;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.string.StringUtils;

/**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>

 */
public class RSAUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";//SHA1withRSA   MD5withRSA

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";


    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair(int keysize) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keysize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }
    
    public static Map<String, Object> genKeyPair() throws Exception {
    	return genKeyPair(1024);
    }
    
    
    /**
     * 获取密钥长度
     * @param key 密钥
     * @return 密钥长度（位）
     */
    private static int getKeyLength(Key key) {
        try {
            // 通过反射获取密钥长度（简化处理）
            // 实际应用中应使用更可靠的方式获取密钥长度
            if (key instanceof java.security.interfaces.RSAPublicKey) {
                return ((java.security.interfaces.RSAPublicKey) key).getModulus().bitLength();
            } else if (key instanceof java.security.interfaces.RSAPrivateKey) {
                return ((java.security.interfaces.RSAPrivateKey) key).getModulus().bitLength();
            }
            return 2048; // 默认值
        } catch (Exception e) {
            return 2048; // 默认值
        }
    }
    
    /**
     * 根据密钥长度动态计算最大加密块大小
     * @param key 密钥
     * @return 最大加密块大小
     */
    public static int getMaxEncryptBlockSize(Key key) {
        if (key instanceof PublicKey) {
            return calculateMaxBlockSize(key, true);
        } else if (key instanceof PrivateKey) {
            return calculateMaxBlockSize(key, false);
        }
        throw new IllegalArgumentException("不支持的密钥类型");
    }
    
    /**
     * 计算最大块大小
     * @param key 密钥
     * @param isPublic 是否为公钥
     * @return 最大块大小
     */
    private static int calculateMaxBlockSize(Key key, boolean isPublic) {
        try {
            // 获取密钥的模数长度
            int keyLength = getKeyLength(key);
            // RSA填充需要预留空间
            int paddingSize = 11; // PKCS#1 v1.5 填充预留空间
            int maxBlockSize = keyLength / 8 - paddingSize;
            return Math.max(1, maxBlockSize);
        } catch (Exception e) {
            throw new RuntimeException("计算最大块大小失败", e);
        }
    }

    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String signBase64(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64.encodeString(signature.sign());
    }

    public static String signBase64(byte[] data, PrivateKey privateK) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64.encodeString(signature.sign());
    }

    /**
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String signHexString(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Utils.bytes2Hex(signature.sign());
    }
    
    public static boolean verifyHex(byte[] data, String publicKey, String sign)
            throws Exception {
        if (StringUtils.isEmpty(sign))
            return false;
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Utils.hex2Byte(sign));
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verifyBase64(byte[] data, String publicKey, String sign)
            throws Exception {
        if (StringUtils.isEmpty(sign))
            return false;
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64.decode(sign));
    }

    public static boolean verifyBase64(byte[] data, PublicKey publicK, String sign)
            throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64.decode(sign));
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey    私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        
        int keyLength = getKeyLength(privateK);
        int maxBlockSize = keyLength / 8;
        
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(encryptedData, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param publicKey     公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        
        int keyLength = getKeyLength(publicK);
        int maxBlockSize = keyLength / 8;
        
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(encryptedData, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        
        int maxBlockSize = getMaxEncryptBlockSize(publicK);
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(data, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        
        int maxBlockSize = getMaxEncryptBlockSize(privateK);
        
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(data, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64.encodeString(key.getEncoded());
    }

    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return Base64.encodeString(key.getEncoded());
    }

    public static void main(String[] args) throws Exception {
        String publicKeyBase64;
        String privateKeyBase64;

        Map<String, Object> mp = genKeyPair(2048);
        RSAPrivateKey privateKey = (RSAPrivateKey) mp.get(PRIVATE_KEY);
        RSAPublicKey publicKey = (RSAPublicKey) mp.get(PUBLIC_KEY);
        publicKeyBase64 = Base64.encodeString(publicKey.getEncoded());
        privateKeyBase64  = Base64.encodeString(privateKey.getEncoded());

        //x509
//        publicKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLBoDV0NzxSt3ooTjUGoshi/CDYTgtps+w7xQM0FEltdXNPnz/V7JC5XDnOMNExTK7Al+vvZf1+S7oqNUK63yAwWywBk/NAXMYiEtUayGCmVyGs5sU2ZRgTxuqk/pt5O+jK+oF7CXZibWjuyiRnxujtoGnk/JJLT1ZbWaGfJd2YQIDAQAB";
        //pkcs8
//        privateKeyBase64 = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsGgNXQ3PFK3eihONQaiyGL8INhOC2mz7DvFAzQUSW11c0+fP9XskLlcOc4w0TFMrsCX6+9l/X5Luio1QrrfIDBbLAGT80BcxiIS1RrIYKZXIazmxTZlGBPG6qT+m3k76Mr6gXsJdmJtaO7KJGfG6O2gaeT8kktPVltZoZ8l3ZhAgMBAAECgYAs5VC9u9Fd6rt24HmlpVzA3JwzETY/J9tyXAKxkKRj9TCyMzfSjdN3Xvjy8pW8o6IaWk3kRunH+Ux4vezutdVij7LWgIFMwlb34dGrvTh4p9VLHlg4zMfld0rmHA85rJV25KAV2TaDYtVj9zt3ne+7/IDnsnGVwb1t5ZbCQ3ZhAQJBAP638SoCW+bHcLfvU0WKTvfJrGK+OWgNzrAwl+26DAD6pynavurOgJFOc4TjRAmIcK29byzegLngu25HbWWoYMkCQQCLuY6tkN+25+pIuFnMwUXqqIqhuG8yOxxlYpzlLtis9Gj99ZqY4CueQRODQbAsioPt83DTie825D33Ut7ntAzZAkAUwDFyZazM+TRyl5mmEaVrRE+535tXocBT1DGcWUq/DaxumXZyNI5+x/BbRBR33linnsYe1qWP/wHl8wWzgWsZAkBYKQlsXDe7yZLatW77sYsy54spRRzsHb3p/pGuTaOrZT+F1dzozrDW1orAS8ckI3XgwrSS1LpZ8SNkFSbOHZ3pAkB/mq/5IHvH+17eBxSU2XG/gFV5BlTZ1S4E+UMuCebqrsSYSjS2VYiePbipZXgW3pLg8lz6FUidkAgo3279+0D3";
        System.out.println("publicKeyBase64: " + publicKeyBase64);
        System.out.println("privateKeyBase64: " + privateKeyBase64);
        String faceId;
        faceId = "11111111111111111111111111111111111";
        //加密
        byte[] encrypt = encryptByPublicKey(faceId.getBytes("utf-8"), publicKeyBase64);
        String encryptString = Base64.encodeString(encrypt);
        System.out.println("送给银商的faceId：" + encryptString);

        //解密
        byte[] decrypt = decryptByPrivateKey(Base64.decode(encryptString), privateKeyBase64);
        System.out.println(new String(decrypt));
        
        
        String sign = signHexString(faceId.getBytes(), privateKeyBase64);
        System.out.println("sign:" + sign);
        
        boolean b = verifyHex(faceId.getBytes(), publicKeyBase64, sign);
        System.out.println("verify sign:" + b);
    }
}