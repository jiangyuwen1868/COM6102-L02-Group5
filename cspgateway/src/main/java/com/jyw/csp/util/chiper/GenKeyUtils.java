package com.jyw.csp.util.chiper;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.jyw.csp.util.Utils;



public class GenKeyUtils {
	
	/**
	 * 随机产生AppKey
	 * @param length 产生AppKey长度
	 * @return
	 */
	public static String genAppKey(int length) {
		SecureRandom random = new SecureRandom();
		byte[] bs = new byte[length];
		random.nextBytes(bs);
		String rs = new BigInteger(bs).abs().toString(10);
		if(rs.length()>=length) {
			return rs.substring(0, length);
		}
		return Utils.fillCharsToStringRight(rs, (char) 48, length);
	}

	/**
	 * 产生AES对称密钥，默认长度 128bit
	 * @return
	 */
	public static String genSecretKey() {
		KeyGenerator keyGenerator = null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] keyBytes = secretKey.getEncoded();
		
		return Utils.bytes2Hex(keyBytes);
	}
	
	/**
	 * 产生RSA密钥对，默认长度1408bit
	 * @return
	 */
	public static SKeyPair genKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			
			RSAKeyGenParameterSpec params = new RSAKeyGenParameterSpec(1408,BigInteger.valueOf(3));
			keyGen.initialize(params, new SecureRandom());
			KeyPair keyPair = keyGen.genKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			
			return new SKeyPair(Utils.bytes2Hex(privateKey.getEncoded()),Utils.bytes2Hex(publicKey.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void test() {
		System.out.println(GenKeyUtils.genSecretKey());
		System.out.println(GenKeyUtils.genKeyPair().getPublicKey());
		System.out.println(GenKeyUtils.genKeyPair().getPrivateKey());
	}
	
	public static void main(String[] args) {
		//test();
	}
}

