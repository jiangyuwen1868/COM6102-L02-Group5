package com.jyw.csp.util.chiper;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;



public class SM4Util {
	public String secretKey = "";
	public String iv = "";
	public boolean hexString = false;

	public SM4Util() {
	}

	/**
	 * SM4_ECB模式加密数据
	 * @param plainText 明文数据
	 * @return Base64编码格式密文
	 */
	public String encryptDataToB4_ECB(String plainText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_ENCRYPT;

			byte[] keyBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
			} else {
				keyBytes = secretKey.getBytes();
			}
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_enc(ctx, keyBytes);
			byte[] encrypted = sm4.sm4_crypt_ecb(ctx,
					plainText.getBytes("UTF-8"));
			String cipherText = new String(Base64.encode(encrypted), "UTF-8");
			if (cipherText != null && cipherText.trim().length() > 0) {
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(cipherText);
				cipherText = m.replaceAll("");
			}
			return cipherText;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SM4_ECB解密Base64编码格式密文
	 * @param cipherText Base64编码格式密文
	 * @return 明文（UTF-8编码）
	 */
	public String decryptDataB4_ECB(String cipherText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_DECRYPT;

			byte[] keyBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
			} else {
				keyBytes = secretKey.getBytes();
			}
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_dec(ctx, keyBytes);
			byte[] decrypted = sm4.sm4_crypt_ecb(ctx,
					Base64.decode(cipherText));
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * SM4_ECB模式加密数据
	 * @param plainText 明文
	 * @return 十六进制格式编码数据
	 */
	public String encryptDataToHex_ECB(String plainText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_ENCRYPT;

			byte[] keyBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
			} else {
				keyBytes = secretKey.getBytes();
			}
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_enc(ctx, keyBytes);
			byte[] encrypted = sm4.sm4_crypt_ecb(ctx,
					plainText.getBytes("UTF-8"));
			
			return Utils.bytes2Hex(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SM4_ECB模式解密十六进制字符串密文数据
	 * @param cipherText 十六进制字符串密文数据
	 * @return 明文（UTF-8编码）
	 */
	public String decryptDataHex_ECB(String cipherText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_DECRYPT;

			byte[] keyBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
			} else {
				keyBytes = secretKey.getBytes();
			}
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_dec(ctx, keyBytes);
			byte[] decrypted = sm4.sm4_crypt_ecb(ctx,
					Utils.hex2Byte(cipherText));
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SM4_CBC加密数据
	 * @param plainText 明文
	 * @return Base64格式编码密文数据
	 */
	public String encryptDataToB4_CBC(String plainText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_ENCRYPT;

			byte[] keyBytes;
			byte[] ivBytes;

			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
				ivBytes = Utils.hex2Byte(iv);
			} else {
				keyBytes = secretKey.getBytes();
				ivBytes = iv.getBytes();
			}

			SM4 sm4 = new SM4();
			sm4.sm4_setkey_enc(ctx, keyBytes);
			byte[] encrypted = sm4.sm4_crypt_cbc(ctx, ivBytes,
					plainText.getBytes("UTF-8"));
			String cipherText = new String(Base64.encode(encrypted), "UTF-8");
			if (cipherText != null && cipherText.trim().length() > 0) {
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(cipherText);
				cipherText = m.replaceAll("");
			}
			return cipherText;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SM4_CBC解密Base64密文数据
	 * @param cipherText Base64密文数据
	 * @return 明文（UTF-8编码）
	 */
	public String decryptDataB4_CBC(String cipherText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_DECRYPT;

			byte[] keyBytes;
			byte[] ivBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
				ivBytes = Utils.hex2Byte(iv);
			} else {
				keyBytes = secretKey.getBytes();
				ivBytes = iv.getBytes();
			}

			SM4 sm4 = new SM4();
			sm4.sm4_setkey_dec(ctx, keyBytes);
			byte[] decrypted = sm4.sm4_crypt_cbc(ctx, ivBytes,
					Base64.decode(cipherText));
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * SM4_CBC加密数据
	 * @param plainText 明文
	 * @return 十六进制字符串密文
	 */
	public String encryptDataToHex_CBC(String plainText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_ENCRYPT;

			byte[] keyBytes;
			byte[] ivBytes;

			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
				ivBytes = Utils.hex2Byte(iv);
			} else {
				keyBytes = secretKey.getBytes();
				ivBytes = iv.getBytes();
			}

			SM4 sm4 = new SM4();
			sm4.sm4_setkey_enc(ctx, keyBytes);
			byte[] encrypted = sm4.sm4_crypt_cbc(ctx, ivBytes,
					plainText.getBytes("UTF-8"));
			
			return Utils.bytes2Hex(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SM4_CBC解密十六进制密文数据
	 * @param cipherText 十六进制密文数据
	 * @return 明文（UTF-8编码）
	 */
	public String decryptDataHex_CBC(String cipherText) {
		try {
			SM4Context ctx = new SM4Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_DECRYPT;

			byte[] keyBytes;
			byte[] ivBytes;
			if (hexString) {
				keyBytes = Utils.hex2Byte(secretKey);
				ivBytes = Utils.hex2Byte(iv);
			} else {
				keyBytes = secretKey.getBytes();
				ivBytes = iv.getBytes();
			}

			SM4 sm4 = new SM4();
			sm4.sm4_setkey_dec(ctx, keyBytes);
			byte[] decrypted = sm4.sm4_crypt_cbc(ctx, ivBytes,
					Utils.hex2Byte(cipherText));
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 生成SM4密钥（HexString）
	 * @return 十六进制字符串SM4密钥
	 */
	public String generateKey() {
		try {
			SecureRandom rng = new SecureRandom();
			BigInteger keyBit = new BigInteger(128, rng);
			while(keyBit.bitLength() < 128){
				keyBit = new BigInteger(128, rng);
			}
			String keyR = "0000000000" + keyBit.toString(16);
			String key = keyR.substring(keyR.length()-32, keyR.length());
			return key.toUpperCase();
		} catch(Exception e) {
			return randomKey(32/2, true);
		}
	}
	
	/**
	 * 生成SM4密钥（AsciiString）
	 * @return ASCII字符串SM4密钥
	 */
	public String randomKey() {
		return randomKey(32/2, false);
	}
	
	protected String randomKey(int size, boolean hexString) {
		String[] seed = new String[] { "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g",
				"h", "i", "j", "k", "m", "n", "p", "Q", "r", "s", "t", "u",
				"v", "w", "x", "y", "z", "2", "3", "4", "5", "6", "7", "8", "9" };// 数组
		int seedlength = seed.length;// 数组长度
		String createPassword = "";
		for (int i = 0; i < size; i++) {
			int j = (int) Math.floor(Math.random() * seedlength);
			createPassword += seed[j];
		}
		if (hexString) {
			return Utils.bytes2Hex(createPassword.getBytes());
		} else {
			return createPassword;
		}
	}
}
