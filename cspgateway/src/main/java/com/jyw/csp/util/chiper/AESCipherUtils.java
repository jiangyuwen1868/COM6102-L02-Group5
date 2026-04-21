package com.jyw.csp.util.chiper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;


public class AESCipherUtils {

	public static final String AES_ALG = "AES"; //DESede(3DES),Blowfish
	public static final String DESede_ALG = "DESede";
	public static final String UTF8 = "UTF-8";
	public static final int  KEY_LNG = 128;         //192,256 
	static {
		if(Security.getProperty("BC")==null)
		{
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	/**
	 * 生成密钥，64bits
	 * @return 密钥bytes数组
	 * @throws NoSuchAlgorithmException
	 */
    public static byte[] getKeyByte() throws NoSuchAlgorithmException{
        KeyGenerator kg=KeyGenerator.getInstance(AES_ALG);
        kg.init(128);
        return kg.generateKey().getEncoded();
    }
    /**
	 * 生成密钥，64bits
	 * @return 密钥bytes数组
	 * @throws NoSuchAlgorithmException
	 */
    public static String  getKeyHex() throws NoSuchAlgorithmException{
         return Utils.bytes2Hex(getKeyByte());
    }
    
    /**
     * 用已知的密钥加密
     * @param src 原文字节码数组
     * @return 密文字节码数组
     * @throws Exception 
   
     */
    public  static byte[] encrypt(byte[] key,byte[] srcBytes) throws Exception {
        try{   
    	//生成密钥
            SecretKey deskey = new SecretKeySpec(key, AES_ALG);
            //加密
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            byte[] cipherText = new byte[cipher.getOutputSize(srcBytes.length)];

    		int ctLength = cipher.update(srcBytes, 0, srcBytes.length, cipherText,
    				0);

    		ctLength += cipher.doFinal(cipherText, ctLength);

    		return cipherText;
    		
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        	throw new Exception("请求特定的加密算法而它在该环境中不可用："+e.getMessage());
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("请求特定安全性 provider 但它在该环境中不可用："+e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("请求特定填充机制但该环境中未提供时："+e.getMessage());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("用于无效 Key（无效的编码、错误的长度、未初始化等）："+e.getMessage());
		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("提供的输出缓冲区太小而不能存储操作结果："+e.getMessage());
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("提供给块密码的数据长度不正确："+e.getMessage());
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("填充机制而数据又未正确填充："+e.getMessage());
		} 
    }
    /**
     * AES加密字符串src
     * @param src 报文原文
     * @return hex 编码字符串
     * @throws Exception 
     */
    public static String encryptHex(String hexKey,String srcText) throws  Exception{
    	
    	return Utils.bytes2Hex(encrypt(Utils.hex2Byte(hexKey),srcText.getBytes()));
    	
    }
    /**
     * AES加密字符串src
     * @param src 报文原文
     * @return Base64 编码字符串
     * @throws Exception
     */
    public static String encryptToBase64(String hexKey,String srcText) throws  Exception{
    	
    	return new String( Base64.encode(encrypt(Utils.hex2Byte(hexKey),srcText.getBytes(UTF8))) );
    	
    }


    /**
     * 用已知的密钥解密
     * @param src 密文字节码数组
     * @return 原文字节码数组
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchProviderException 
     * @throws ShortBufferException 
     */
    public static byte[] decrypt(byte[] key,byte[] srcBytes) throws Exception {      
          	try{   
    				SecretKey deskey = new SecretKeySpec(key, AES_ALG);
    	           
    	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
    	            cipher.init(Cipher.DECRYPT_MODE, deskey);
    	            byte[] cipherText = new byte[cipher.getOutputSize(srcBytes.length)];

    	    		int ctLength = cipher.update(srcBytes, 0, srcBytes.length, cipherText,
    	    				0);

    	    		ctLength += cipher.doFinal(cipherText, ctLength);

    	    		return cipherText;
    	    		
    	        } catch (NoSuchAlgorithmException e) {
    	        	e.printStackTrace();
    	        	throw new Exception("请求特定的加密算法而它在该环境中不可用："+e.getMessage());
    			} catch (NoSuchProviderException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("请求特定安全性 provider 但它在该环境中不可用："+e.getMessage());
    			} catch (NoSuchPaddingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("请求特定填充机制但该环境中未提供时："+e.getMessage());
    			} catch (InvalidKeyException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("用于无效 Key（无效的编码、错误的长度、未初始化等）："+e.getMessage());
    			} catch (ShortBufferException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("提供的输出缓冲区太小而不能存储操作结果："+e.getMessage());
    			} catch (IllegalBlockSizeException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("提供给块密码的数据长度不正确："+e.getMessage());
    			} catch (BadPaddingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				throw new Exception("填充机制而数据又未正确填充："+e.getMessage());
    			} 
          
    }
    /**
     * AES解密
     * @param src 接收到的报文，hex
     * @return 解密后的报文原文
     * @throws Exception
     */
     
    public static String decryptHex(String hexKey,String cipherHex)throws  Exception{
    	return new String(decrypt(Utils.hex2Byte(hexKey),Utils.hex2Byte(cipherHex)),UTF8);
    	
    }
    /**
     * AES解密
     * @param src 接收到的报文，Base64格式加密密码
     * @return 解密后的报文原文
     * @throws Exception
     */
     
    public static String decryptBase64(String hexKey,String cipherBase64)throws  Exception{
    	return new String(decrypt(Utils.hex2Byte(hexKey),Base64.decode(cipherBase64)),UTF8);
    	
    }
    
//    public static void main(String[] args) throws Exception {
//		String hexKey = "404142434445464748494A4B4C4D4E4C";
//		String srcText = "我爱中国123abc!";
//		String cipherHex = AESCipherUtils.encryptHex(hexKey, srcText);
//		System.out.println("encrypt>> cipherHex:" + cipherHex);
//		
//		srcText = AESCipherUtils.decryptHex(hexKey, cipherHex);
//		System.out.println("decrypt>> srcText:" + srcText);
//		
//		String cipherBase64 = "aBa2v/ScnYRtWi1EtbgAxybDZmzIGnLWaQW8CAnOwfCN7kjc5t7M41WH6IhXPEx9OTVOJq2gqjjVcxSsba/UMWtXK9JCOvaTncu+sUbVa7Mz9vShpmeIuyx5292Bt3jgoS9tVeheUQOEfHj++xntHtJqy1XUbK1VtgywbXfy83u/YWLzcKOvx1N7eT5GXg9MI6LRQZpQsP3xFniwzIR9hl+hC6H6X20Unk7eAuMkiqKw4sYfrooJa8aH2ClMe1p8q41bwhkhEn4BlZUgFRLBJYf5Twh73/axYgEhyvviG28HqsnVy8ld3SBe2Do3gdO2bE/9H6Q/6c/Y/rBGIvYdv1/WVwP/zlEEpJE1xxCHD7HBf2Rvwq1QLNDhwrqhAm/+9vGgDe6maXtpMQAytIvZiViZZS2zc/M6gGsb4W3mINKWs28QeR/XJTFpL05+hHln";
//		srcText = AESCipherUtils.decryptBase64(hexKey, cipherBase64);
//		System.out.println("decrypt>> srcText:" + srcText);
//		
//	}
}
