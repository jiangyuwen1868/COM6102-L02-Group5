package com.jyw.csp.util.chiper;

import java.security.GeneralSecurityException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;


public class DESedeCipherUtils {
	public static final String DESede_ALG="DESede";
	public static final String UTF8 = "UTF-8";
	static {
		if(Security.getProperty("BC")==null)
		{
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	
	public static byte[] decrypt(byte[] secretKey, byte[] cipherByte) throws GeneralSecurityException {
		SecretKeySpec k = new SecretKeySpec(secretKey, DESede_ALG);
		Cipher cp = Cipher.getInstance("DESede/ECB/PKCS7Padding","BC");
		cp.init(Cipher.DECRYPT_MODE, k);
		byte[] srcByte = cp.doFinal(cipherByte);
		return srcByte;
	}
	
	public static byte[] decrypt(byte[] secretKey, byte[] ivs, byte[] cipherByte) throws GeneralSecurityException {
		SecretKeySpec k = new SecretKeySpec(secretKey, DESede_ALG);
		IvParameterSpec iv = new IvParameterSpec(ivs);
		Cipher cp = Cipher.getInstance("DESede/CBC/PKCS7Padding","BC");
		cp.init(Cipher.DECRYPT_MODE, k, iv);
		byte[] srcByte = cp.doFinal(cipherByte);
		return srcByte;
	}

	public static byte[] decrypt(String secretKey, byte[] cipherByte) throws GeneralSecurityException {
		byte[] kb = secretKey.getBytes();
		SecretKeySpec k = new SecretKeySpec(kb, DESede_ALG);
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding","BC");
		cipher.init(Cipher.DECRYPT_MODE, k);
		byte[] srcByte = cipher.doFinal(cipherByte);
		return srcByte;
	}

	public static byte[] encrypt(byte[] secretKey, byte[] srcByte) throws GeneralSecurityException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, DESede_ALG);
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding","BC");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] cipherByte = cipher.doFinal(srcByte);
		return cipherByte;
	}
	
	public static byte[] encrypt(byte[] secretKey, byte[] ivs, byte[] srcByte) throws GeneralSecurityException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, DESede_ALG);
		IvParameterSpec iv = new IvParameterSpec(ivs);
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS7Padding","BC");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
		byte[] cipherByte = cipher.doFinal(srcByte);
		return cipherByte;
	}

	public static byte[] encrypt(String secretKey, byte[] srcByte) throws GeneralSecurityException {
		byte[] kb = secretKey.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(kb, DESede_ALG);
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding","BC");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] cipherByte = cipher.doFinal(srcByte);
		return cipherByte;
	}
	
	public static String encryptHex(String hexKey,String srcText) throws  Exception{
    	return Utils.bytes2Hex(encrypt(Utils.hex2Byte(hexKey),srcText.getBytes()));
    }
	
	public static String encryptBase64(String hexKey,String srcText) throws  Exception{
    	return new String( Base64.encode(encrypt(Utils.hex2Byte(hexKey),srcText.getBytes(UTF8))) );
    }
	
	public static String encryptBase64(String hexKey,String hexIv, String srcText) throws  Exception{
    	return new String( Base64.encode(encrypt(Utils.hex2Byte(hexKey),Utils.hex2Byte(hexIv),srcText.getBytes(UTF8))) );
    }
	
	public static String decryptHex(String hexKey,String cipherHex)throws  Exception{
    	return new String(decrypt(Utils.hex2Byte(hexKey),Utils.hex2Byte(cipherHex)),UTF8);
    }
	
	/**
	 * 3DES解密
	 * @param hexKey 十六进制字符串密钥
	 * @param cipherBase64 Base64编码加密数据密文 
	 * @return 解密后的报文原文
	 * @throws Exception
	 */
	public static String decryptBase64(String hexKey,String cipherBase64)throws  Exception{
    	return new String(decrypt(Utils.hex2Byte(hexKey),Base64.decode(cipherBase64)),UTF8);
    }
	
	
//	public static void main(String[] args) throws Exception {
//		String hexKey = "404142434445464748494A4B4C4D4E4C";
//		String hexIv = "3132333435363738";
//		String srcText = "我爱中国123abc!";
//		String cipherHex = DESedeCipherUtils.encryptHex(hexKey, srcText);
//		System.out.println("encrypt>> cipherHex:" + cipherHex);
//		srcText = DESedeCipherUtils.decryptHex(hexKey, cipherHex);
//		System.out.println("decrypt>> srcText:" + srcText);
//		
//		byte[] cipherByte = DESedeCipherUtils.encrypt(Utils.hex2Byte(hexKey), Utils.hex2Byte(hexIv), srcText.getBytes());
//		System.out.println("encrypt>> cipherHex:" + Utils.bytes2Hex(cipherByte));
//		
//		cipherByte = Base64.decode("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");
//		byte[] srcByte = DESedeCipherUtils.decrypt(Utils.hex2Byte(hexKey), Utils.hex2Byte(hexIv), cipherByte);
//		System.out.println("decrypt>> srcText:" + new String(srcByte));
//	}
}
