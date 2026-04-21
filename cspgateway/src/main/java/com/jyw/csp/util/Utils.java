package com.jyw.csp.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.util.string.StringUtils;

public class Utils {

	public static final String[] EXPR_STARTS = { "${", "%{", "#{" };
	public static final String EXPR_END = "}";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	
	public final static String ENCODE_GB2312 = "GB2312";
	public final static String ENCODE_GBK = "GBK";
	public final static String ENCODE_ISO8859 = "ISO-8859-1";
	public final static String ENCODE_UTF8 = "UTF-8";

	/**
	 * 字符串s不足 length 长度 左补字符c
	 * 
	 * @param s
	 * @param c
	 * @param length
	 * @return
	 */
	public static String fillCharsToStringLeft(String s, char c, int length) {
		return fillCharsToString(s, c, length, LEFT);
	}

	/**
	 * 字符串s不足 length 长度 右补字符c
	 * 
	 * @param s
	 * @param c
	 * @param length
	 * @return
	 */
	public static String fillCharsToStringRight(String s, char c, int length) {
		return fillCharsToString(s, c, length, RIGHT);
	}

	private static String fillCharsToString(String s, char c, int length,
			String flag) {
		if (s == null) {
			s = "";
		}

		if (s.length() >= length) {
			return s;
		}

		int fillLength = length - s.length();
		char[] fillChars = new char[fillLength];
		for (int i = 0; i < fillLength; ++i) {
			fillChars[i] = c;
		}

		String fillString = new String(fillChars);
		if (LEFT.equalsIgnoreCase(flag))
			return new StringBuilder().append(fillString).append(s).toString();
		if (RIGHT.equalsIgnoreCase(flag))
			return new StringBuilder().append(s).append(fillString).toString();

		return s;
	}

	public static Method toSetterMethod(Class<?> clazz, String property) {
		if ((Map.class.isAssignableFrom(clazz))
				|| (Collection.class.isAssignableFrom(clazz))) {
			return null;
		}
		try {
			return new PropertyDescriptor(property, clazz).getWriteMethod();
		} catch (IntrospectionException e) {
			throw new CommonRuntimeException("XATF300100AB", e);
		}
	}

	public static Method toGetterMethod(Class<?> clazz, String property) {
		if ((Map.class.isAssignableFrom(clazz))
				|| (Collection.class.isAssignableFrom(clazz))) {
			return null;
		}
		try {
			return new PropertyDescriptor(property, clazz).getReadMethod();
		} catch (IntrospectionException e) {
			throw new CommonRuntimeException("XATF300100AB", e);
		}
	}

	public static String capitalize(String name) {
		if (!(StringUtils.hasText(name)))
			return name;

		return new StringBuilder()
				.append(name.substring(0, 1).toUpperCase(Locale.ENGLISH))
				.append(name.substring(1)).toString();
	}

	public static Object invokeMethod(Object obj, Method method, Object[] args) {
		try {
			return method.invoke(obj, args);
		} catch (Exception e) {
			String argsString = null;
			if (args != null) {
				StringBuilder sbArgs = new StringBuilder(args.getClass()
						.getName()).append(" {");

				if (args.length > 0) {
					Object[] arr$ = args;
					int len$ = arr$.length;
					for (int i$ = 0; i$ < len$; ++i$) {
						Object arg = arr$[i$];
						sbArgs.append(arg).append(',');
					}

					sbArgs.deleteCharAt(sbArgs.length() - 1);
				}
				argsString = sbArgs.append('}').toString();
			}

			throw new CommonRuntimeException("XATF300100AC", e);
		}
	}

	private static final Map<String, Object> GET_INSTANCE_CACHE = new HashMap<String, Object>();
	public static Object getInstance(String className) {
		Object object = GET_INSTANCE_CACHE.get(className);
		if(object == null) {
			object = getInstance(CacheUtils.getClassForName(className));
			GET_INSTANCE_CACHE.put(className, object);
		}
		return object;
	}

	public static Object getInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new CommonRuntimeException("XATF300100AD", e);
		}
	}

	public static boolean isExpression(String plainExpr) {
		if (!(StringUtils.hasText(plainExpr))) {
			return false;
		}

		int start = -1;
		int end = -1;
		if ((end = plainExpr.lastIndexOf("}")) != -1) {
			String[] arr$ = EXPR_STARTS;
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; ++i$) {
				String expStart = arr$[i$];
				if (((start = plainExpr.indexOf(expStart)) != -1)
						&& (start < end))
					return true;
			}

		}

		return false;
	}

	public static String parseExpression(String plainExpr) {
		if (!(StringUtils.hasText(plainExpr))) {
			return plainExpr;
		}

		int start = -1;
		int end = -1;
		if ((end = plainExpr.lastIndexOf("}")) != -1) {
			String[] arr$ = EXPR_STARTS;
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; ++i$) {
				String expStart = arr$[i$];
				if (((start = plainExpr.indexOf(expStart)) != -1)
						&& (start < end))
					return plainExpr.substring(start + expStart.length(), end);
			}

		}

		return plainExpr;
	}

	/**
	 * 获取请求客户端IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if ((ip == null) || (ip.length() == 0)
				|| ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("Proxy-Client-IP");

		if ((ip == null) || (ip.length() == 0)
				|| ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("WL-Proxy-Client-IP");

		if ((ip == null) || (ip.length() == 0)
				|| ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("HTTP_CLIENT_IP");

		if ((ip == null) || (ip.length() == 0)
				|| ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");

		if ((ip == null) || (ip.length() == 0)
				|| ("unknown".equalsIgnoreCase(ip)))
			ip = request.getRemoteAddr();

		return ip;
	}

	public static byte[] getByteArray(String hexString) {
		byte[] hexbyte = hexString.getBytes();
		byte[] bitmap = new byte[hexbyte.length / 2];
		for (int i = 0; i < bitmap.length; i++) {
			hexbyte[i * 2] -= hexbyte[i * 2] > '9' ? 7 : 0;
			hexbyte[i * 2 + 1] -= hexbyte[i * 2 + 1] > '9' ? 7 : 0;
			bitmap[i] = (byte) ((hexbyte[i * 2] << 4 & 0xf0) | (hexbyte[i * 2 + 1] & 0x0f));
		}
		return bitmap;
	}

	/**
	 * 十六进制字符串转为byte数字
	 * 
	 * @param hexStr
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static byte[] hex2Byte(String hexStr)
			throws IllegalArgumentException {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return bytes;
	}

	/**
	 * 二进制数组转十六进制字符串
	 * 
	 * @param bytes
	 *            byte[]
	 * @return String
	 */
	public static String bytes2Hex(byte[] bytes) {
		String str = "0123456789ABCDEF";
		char[] hexChar = str.toCharArray();
		char[] sb = new char[bytes.length * 2];
		for (int i = 0,j=0; i < bytes.length; i++) {
			sb[j] = (hexChar[(bytes[i] & 0xf0) >>> 4]);
			sb[j+1] = (hexChar[bytes[i] & 0x0f]);
			j = j + 2;
		}
		return new String(sb);
	}

	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < b.length; i++) {
			int temp = b[i] & 0xFF;
			value = value * 256;
			value = value + temp;
		}
		return value;
	}

	/**
	 * 把HEX转换成char 数组
	 * 
	 * @param hexStr
	 * @return
	 */
	public static char[] HexToChar(String hexStr) {
		int len = hexStr.length() / 2;
		String temp;
		char[] outInt = new char[len];
		for (int i = 0; i < len; i++) {
			temp = hexStr.substring(2 * i, 2 * i + 2);
			outInt[i] = (char) java.lang.Integer.parseInt(temp, 16);
		}
		return outInt;
	}

	/**
	 * 异或计算
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] byteXor(byte[] input) {
		int len = input.length / 2;

		char[] inputChar = new char[input.length];
		char[] outChar = new char[len];
		byte[] outByte = new byte[len];

		for (int i = 0; i < input.length; i++) {
			inputChar[i] = (char) input[i];
		}
		for (int i = 0; i < len; i++) {
			outChar[i] = (char) (inputChar[i] ^ inputChar[len + i]);
		}
		for (int i = 0; i < len; i++) {
			outByte[i] = (byte) outChar[i];
		}
		return outByte;
	}

	/**
	 * int类型转为十六进制字符串
	 * 
	 * @param n
	 * @return
	 */
	public static String toHexString(int n) {
		return Integer.toHexString(n).toUpperCase();
	}

	/**
	 * 十六进制数字字符串转为int类型
	 * 
	 * @param hexString
	 * @return
	 */
	public static int hex2Int(String hexString) {
		return Integer.valueOf(hexString, 16);
	}

	/**
	 * 获取GUID标示
	 * 
	 * @return　返回GUID标示号码
	 */
	public static final String GUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replaceAll("-", "");
	}
	
	public static final InputStream byte2Input(byte[] buf) {  
       return new ByteArrayInputStream(buf);  
    }  
  
    public static final byte[] input2byte(InputStream inStream)  
            throws IOException {  
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
        byte[] buff = new byte[100];  
        int rc = 0;  
        while ((rc = inStream.read(buff, 0, 100)) > 0) {  
            swapStream.write(buff, 0, rc);  
        }  
        byte[] in2b = swapStream.toByteArray();  
        return in2b;  
    }
    
    public static String decodeSM2SignDerByB64(String signDerB64) {
    	Assert.notNull(signDerB64, "签名结果不能为空");
    	String signHex = Utils.bytes2Hex(Base64.decode(signDerB64.getBytes()));
    	return decodeSM2SignDerByHex(signHex);
    }
    public static String decodeSM2SignDerByHex(String signDerHex) {
    	Assert.notNull(signDerHex, "签名结果不能为空");
    	StringBuffer signMsg = new StringBuffer();
    	
    	int offset = 0;
		
		int length = 1*2;
		String derTag = signDerHex.substring(offset, offset+length);
		offset += length;
		
		if(!derTag.startsWith("30")) {
    		Assert.isNull(signDerHex, "签名结果格式错误");
    	}
		
		length = 1*2;
		int derLength = Utils.hex2Int(signDerHex.substring(offset, offset+length));
		offset += length;
		
    	String derValue = signDerHex.substring(offset, offset+derLength*2);
    	
    	offset = 0;
    	//signR
    	length = 1*2;
		String rTag = derValue.substring(offset, offset+length);
		offset += length;
		
		length = 1*2;
		int rLength = Utils.hex2Int(derValue.substring(offset, offset+length));
		offset += length; 
		
		String rValue = derValue.substring(offset, offset+rLength*2);
		offset += rLength*2;
    	//signS
		length = 1*2;
		String sTag = derValue.substring(offset, offset+length);
		offset += length;
		
		length = 1*2;
		int sLength = Utils.hex2Int(derValue.substring(offset, offset+length));
		offset += length; 
		
		String sValue = derValue.substring(offset, offset+sLength*2);
		
		if(rValue.length()>64) {
			rValue = rValue.substring(rValue.length()-64);
		}
		rValue = Utils.fillCharsToStringLeft(rValue, '0', 64);
		if(sValue.length()>64) {
			sValue = sValue.substring(sValue.length()-64);
		}
		sValue = Utils.fillCharsToStringLeft(sValue, '0', 64);
		signMsg.append(rValue).append(sValue);
    	return signMsg.toString();
    }
    
    public static String int2hexstring(Integer a, Integer nLen) {
		String sFromt = "%0" + nLen + "X";

		return String.format(sFromt, a);
	}

    /**
     * US7ASCII编码转UTF-8编码
     * @param str
     * @return
     */
    public static String iso2utf(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			String tmp = new String(str.getBytes(ENCODE_ISO8859),ENCODE_GBK);
			return new String(tmp.getBytes(ENCODE_UTF8),ENCODE_UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * UTF-8编码转US7ASCII编码
     * @param str
     * @return
     */
    public static String utf2iso(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			String tmp = new String(str.getBytes(ENCODE_UTF8),ENCODE_GBK);
			return new String(tmp.getBytes(ENCODE_GBK),ENCODE_ISO8859);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * GBK编码转ISO-8859-1编码
     * @param str
     * @return
     */
    public static String gbk2iso(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			return new String(str.getBytes(ENCODE_GBK),ENCODE_ISO8859);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * ISO-8859-1编码转GBK编码
     * @param str
     * @return
     */
    public static String iso2gbk(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			return new String(str.getBytes(ENCODE_ISO8859),ENCODE_GBK);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * GBK编码转UTF-8编码
     * @param str
     * @return
     */
    public static String gbk2utf(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			return new String(str.getBytes(ENCODE_GBK),ENCODE_UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * UTF-8编码转GBK编码
     * @param str
     * @return
     */
    public static String utf2gbk(String str) {
    	if (str==null) {
    		return null;
    	}
    	try {
			return new String(str.getBytes(ENCODE_UTF8),ENCODE_GBK);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
    }
    
    /**
     * 获取字符编码
     * @param str
     * @return
     */
    public static String getEncoding(String str) {
		String encode = ENCODE_GBK;
		if(str==null){
			return null;
		}
		try {
			if(str.equals(new String(str.getBytes(encode),encode))){
				return encode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		encode = ENCODE_GB2312;
		try {
			if(str.equals(new String(str.getBytes(encode),encode))){
				return encode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		encode = ENCODE_ISO8859;
		try {
			if(str.equals(new String(str.getBytes(encode),encode))){
				return encode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		encode = ENCODE_UTF8;
		try {
			if(str.equals(new String(str.getBytes(encode),encode))){
				return encode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encode;
	}
    
    /**
	 * 兼容数据库编码和页面编码处理
	 * GBK转ISO
	 * ISO转GBK
	 * @param value 转码后的值
	 * @return
	 */
	public static String getSetAfterValue(String value) {
		if(value==null)return null;
		if(ENCODE_GBK.equals(getEncoding(value))) {
			try {
				return new String(value.getBytes(ENCODE_GBK),ENCODE_ISO8859);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(ENCODE_ISO8859.equals(getEncoding(value))) {
			try {
				return new String(value.getBytes(ENCODE_ISO8859),ENCODE_GBK);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(ENCODE_GB2312.equals(getEncoding(value))) {
			try {
				return new String(value.getBytes(ENCODE_GB2312),ENCODE_ISO8859);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(ENCODE_UTF8.equals(getEncoding(value))) {
			try {
				value = new String(value.getBytes(ENCODE_UTF8),ENCODE_GBK);
				return new String(value.getBytes(ENCODE_GBK),ENCODE_ISO8859);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	
	/*
     * 中文转unicode编码
     */
    public static String unicodeEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            unicodeBytes = unicodeBytes + "\\u" + Utils.fillCharsToStringLeft(hexB, (char)48, 4);
        }
        return unicodeBytes;
    }
    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }
}
