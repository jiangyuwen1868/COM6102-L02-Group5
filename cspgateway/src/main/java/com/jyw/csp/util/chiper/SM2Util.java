package com.jyw.csp.util.chiper;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.digests.ShortenedDigest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.KDF1BytesGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ISO18033KDFParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import com.jyw.csp.util.Assert;
import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;

public class SM2Util {

	static class SM2 {
		private static String[] eccParams = {
				"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
				"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
				"28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
				"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
				"32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
				"BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0" };

		protected BigInteger eccP;
		protected BigInteger eccA;
		protected BigInteger eccB;
		protected BigInteger eccGx;
		protected BigInteger eccGy;
		protected BigInteger eccN;
		protected ECPoint eccPointG;
		protected ECCurve eccCurve;
		protected ECDomainParameters eccBcSpec;
		protected ECKeyPairGenerator eccKeyPairGenerator;
		protected ECFieldElement eccGxFieldElement;
		protected ECFieldElement eccGyFieldElement;

		public SM2() {
			this.eccP = new BigInteger(eccParams[0], 16);
			this.eccA = new BigInteger(eccParams[1], 16);
			this.eccB = new BigInteger(eccParams[2], 16);
			this.eccGx = new BigInteger(eccParams[4], 16);
			this.eccGy = new BigInteger(eccParams[5], 16);
			this.eccN = new BigInteger(eccParams[3], 16);

			this.eccCurve = new ECCurve.Fp(eccP, eccA, eccB);

			this.eccGxFieldElement = eccCurve.fromBigInteger(eccGx);
			this.eccGyFieldElement = eccCurve.fromBigInteger(eccGy);

			this.eccPointG = eccCurve.createPoint(eccGx, eccGy);

			this.eccBcSpec = new ECDomainParameters(eccCurve, eccPointG, eccN);

			ECKeyGenerationParameters param = new ECKeyGenerationParameters(
					eccBcSpec, new SecureRandom());

			this.eccKeyPairGenerator = new ECKeyPairGenerator();
			this.eccKeyPairGenerator.init(param);
		}
	}

	// 生成非对称密钥对，公钥、私钥
	public static Map<String, String> generateKeyPairByJava() {
		SM2 sm2 = new SM2();
		AsymmetricCipherKeyPair key = sm2.eccKeyPairGenerator.generateKeyPair();
		ECPrivateKeyParameters ecPriv = (ECPrivateKeyParameters) key
				.getPrivate();
		ECPublicKeyParameters ecPub = (ECPublicKeyParameters) key.getPublic();
		BigInteger privateKey = ecPriv.getD();
		ECPoint publicKey = ecPub.getQ();
		
//		if(!checkPublicKey(publicKey)) {
//			
//		}

		Map<String, String> keyPairMap = new HashMap<String, String>();
		String publicKeyTmp = Utils.bytes2Hex(publicKey.getEncoded(false));
		keyPairMap.put("publicKey",
				publicKeyTmp.substring(2, publicKeyTmp.length()));
		keyPairMap.put("privateKey",
				Utils.bytes2Hex(asUnsignedByteArray(privateKey)));

		return keyPairMap;
	}

	static byte[] getENTL(byte[] bytes) {
		byte[] entlByte = new byte[2];
		int i = bytes.length * 8;
		entlByte[1] = ((byte) (i & 0xFF));
		entlByte[0] = ((byte) (i >> 8 & 0xFF));
		return entlByte;
	}

	static byte[] asUnsignedByteArray(BigInteger value) {
		byte bytes[] = value.toByteArray();
		if (bytes[0] == 0) {
			byte tmp[] = new byte[bytes.length - 1];
			System.arraycopy(bytes, 1, tmp, 0, tmp.length);
			return tmp;
		} else {
			return bytes;
		}
	}

	static byte[] getT1(SM2 sm2, String publicKey) {
		String id = "1234567812345678";
		byte[] idByte = id.getBytes();
		byte[] entlByte = getENTL(idByte);

		byte[] aByte = asUnsignedByteArray(sm2.eccA);
		byte[] bByte = asUnsignedByteArray(sm2.eccB);
		byte[] gxByte = asUnsignedByteArray(sm2.eccGx);
		byte[] gyByte = asUnsignedByteArray(sm2.eccGy);
		byte[] axyByte = Utils.hex2Byte(publicKey);

		byte[] t1Bytes = new byte[entlByte.length + idByte.length
				+ aByte.length + bByte.length + gxByte.length + gyByte.length
				+ axyByte.length];
		System.arraycopy(entlByte, 0, t1Bytes, 0, entlByte.length);
		System.arraycopy(idByte, 0, t1Bytes, entlByte.length, idByte.length);
		System.arraycopy(aByte, 0, t1Bytes, entlByte.length + idByte.length,
				aByte.length);
		System.arraycopy(bByte, 0, t1Bytes, entlByte.length + idByte.length
				+ aByte.length, bByte.length);
		System.arraycopy(gxByte, 0, t1Bytes, entlByte.length + idByte.length
				+ aByte.length + bByte.length, gxByte.length);
		System.arraycopy(gyByte, 0, t1Bytes, entlByte.length + idByte.length
				+ aByte.length + bByte.length + gxByte.length, gyByte.length);
		System.arraycopy(axyByte, 0, t1Bytes, entlByte.length + idByte.length
				+ aByte.length + bByte.length + gxByte.length + gyByte.length,
				axyByte.length);

		return t1Bytes;
	}

	// 签名数据
	public static String signByJava(String publicKey, String privateKey,
			String data) {
		SM2 sm2 = new SM2();
		byte[] t1Bytes = getT1(sm2, publicKey);

		SM3Digest sm3Digest = new SM3Digest();
		sm3Digest.update(t1Bytes, 0, t1Bytes.length);
		byte[] zBytes = new byte[32];
		sm3Digest.doFinal(zBytes, 0);

		byte[] dataBytes = data.getBytes();

		byte[] t2Bytes = new byte[zBytes.length + dataBytes.length];
		System.arraycopy(zBytes, 0, t2Bytes, 0, zBytes.length);
		System.arraycopy(dataBytes, 0, t2Bytes, zBytes.length, dataBytes.length);

		sm3Digest.update(t2Bytes, 0, t2Bytes.length);
		byte[] hashBytes = new byte[32];
		sm3Digest.doFinal(hashBytes, 0);
		BigInteger e = new BigInteger(1, hashBytes);
		BigInteger k = null;
		ECPoint pointK = null;
		BigInteger r = null;
		BigInteger s = null;
		BigInteger d = null;

		ECPrivateKeyParameters ecPriv = new ECPrivateKeyParameters(
				new BigInteger(privateKey, 16), sm2.eccBcSpec);
		SecureRandom random = new SecureRandom();
		do {
			do {
				do {
					k = new BigInteger(sm2.eccN.bitLength(), random);
				} while (k.equals(BigInteger.ZERO)
						|| k.compareTo(sm2.eccN) >= 0);

				pointK = sm2.eccPointG.multiply(k);

				d = ecPriv.getD();

				r = e.add(pointK.normalize().getXCoord().toBigInteger());
				r = r.mod(sm2.eccN);
			} while (r.equals(BigInteger.ZERO) || r.add(k).equals(sm2.eccN));

			BigInteger da1 = d.add(BigInteger.ONE);
			da1 = da1.modInverse(sm2.eccN);

			s = r.multiply(d);
			s = k.subtract(s).mod(sm2.eccN);
			s = da1.multiply(s).mod(sm2.eccN);
		} while (s.equals(BigInteger.ZERO));

		String signR = Utils.bytes2Hex(asUnsignedByteArray(r));
		signR = Utils.fillCharsToStringLeft(signR, '0', 64);
		String signS = Utils.bytes2Hex(asUnsignedByteArray(s));
		signS = Utils.fillCharsToStringLeft(signS, '0', 64);

		return signR + signS;
	}

	// 验证签名数据
	public static boolean verifyByJava(String publicKey, String data,
			String signData) {
		SM2 sm2 = new SM2();

		byte[] t1Bytes = getT1(sm2, publicKey);

		SM3Digest sm3Digest = new SM3Digest();
		sm3Digest.update(t1Bytes, 0, t1Bytes.length);
		byte[] zBytes = new byte[32];
		sm3Digest.doFinal(zBytes, 0);

		byte[] dataBytes = data.getBytes();

		byte[] t2Bytes = new byte[zBytes.length + dataBytes.length];
		System.arraycopy(zBytes, 0, t2Bytes, 0, zBytes.length);
		System.arraycopy(dataBytes, 0, t2Bytes, zBytes.length, dataBytes.length);

		sm3Digest.update(t2Bytes, 0, t2Bytes.length);
		byte[] hashBytes = new byte[32];
		sm3Digest.doFinal(hashBytes, 0);

		BigInteger ax = new BigInteger(publicKey.substring(0,
				publicKey.length() / 2), 16);
		BigInteger ay = new BigInteger(
				publicKey.substring(publicKey.length() / 2), 16);

		ECPoint pointA = sm2.eccCurve.createPoint(ax, ay);

		BigInteger r = new BigInteger(signData.substring(0,
				signData.length() / 2), 16);
		BigInteger s = new BigInteger(
				signData.substring(signData.length() / 2), 16);

		if (r.equals(BigInteger.ZERO) || r.compareTo(sm2.eccN) >= 0) {
			return false;
		}
		if (s.equals(BigInteger.ZERO) || s.compareTo(sm2.eccN) >= 0) {
			return false;
		}

		BigInteger e = new BigInteger(1, hashBytes);

		BigInteger t = r.add(s).mod(sm2.eccN);

		if (t.equals(BigInteger.ZERO)) {
			return false;
		}

		ECPoint x1y1 = sm2.eccPointG.multiply(s);
		x1y1 = x1y1.add(pointA.multiply(t));

		BigInteger rr = e.add(x1y1.normalize().getXCoord().toBigInteger()).mod(
				sm2.eccN);
		return r.equals(rr);
	}
	
	public static String sign2DerHex(String publicKey, String privateKey,
			String data) {
		String signHex = signByJava(publicKey, privateKey, data);
		Assert.notNull(signHex, "签名失败");
		String r = signHex.substring(0, signHex.length()/2);
		String s = signHex.substring(signHex.length()/2);
		//首字节高位大于8,前一字节补00
		StringBuffer derHex = new StringBuffer("30");
		if("00".equals(r.substring(0,2))) {
			r = r.substring(2);
		}
		if(Utils.hex2Int(r.substring(0,1))>=8) {
			r = "00" + r;
		} 
		if("00".equals(s.substring(0,2))) {
			s = s.substring(2);
		}
		if(Utils.hex2Int(s.substring(0,1))>=8) {
			s = "00" + s;
		}
		String seqR = "02" + Utils.int2hexstring(r.length()/2, 2) + r;
		String seqS = "02" + Utils.int2hexstring(s.length()/2, 2) + s;
		derHex.append(Utils.int2hexstring((seqR+seqS).length()/2, 2)).append(seqR).append(seqS);
		return derHex.toString();
	}
	
	public static String sign2DerB64(String publicKey, String privateKey,
			String data) {
		String derHex = sign2DerHex(publicKey, privateKey, data);
		return new String(Base64.encode(Utils.hex2Byte(derHex)));
	}
	
	public static boolean verifySignByDerHex(String publicKey, String data,
			String signDataDerHex) {
		String signData = Utils.decodeSM2SignDerByHex(signDataDerHex);
		return verifyByJava(publicKey, data, signData);
	}
	
	public static boolean verifySignByDerB64(String publicKey, String data,
			String signDataDerB64) {
		String signData = Utils.decodeSM2SignDerByB64(signDataDerB64);
		return verifyByJava(publicKey, data, signData);
	}

	public static String signBySM2(String privateKey, String data) {
		SM2 sm2 = new SM2();

		byte[] hashBytes = data.getBytes();

		BigInteger e = new BigInteger(1, hashBytes);
		BigInteger k = null;
		ECPoint pointK = null;
		BigInteger r = null;
		BigInteger s = null;
		BigInteger d = null;

		ECPrivateKeyParameters ecPriv = new ECPrivateKeyParameters(
				new BigInteger(privateKey, 16), sm2.eccBcSpec);
		SecureRandom random = new SecureRandom();
		do {
			do {
				do {
					k = new BigInteger(sm2.eccN.bitLength(), random);
				} while (k.equals(BigInteger.ZERO)
						|| k.compareTo(sm2.eccN) >= 0);

				pointK = sm2.eccPointG.multiply(k);

				d = ecPriv.getD();

				r = e.add(pointK.normalize().getXCoord().toBigInteger());
				r = r.mod(sm2.eccN);
			} while (r.equals(BigInteger.ZERO) || r.add(k).equals(sm2.eccN));

			BigInteger da1 = d.add(BigInteger.ONE);
			da1 = da1.modInverse(sm2.eccN);

			s = r.multiply(d);
			s = k.subtract(s).mod(sm2.eccN);
			s = da1.multiply(s).mod(sm2.eccN);
		} while (s.equals(BigInteger.ZERO));

		String signR = Utils.bytes2Hex(asUnsignedByteArray(r));
		signR = Utils.fillCharsToStringLeft(signR, '0', 64);
		String signS = Utils.bytes2Hex(asUnsignedByteArray(s));
		signS = Utils.fillCharsToStringLeft(signS, '0', 64);

		return signR + signS;
	}

	/**
	 * SM3计算数据摘要
	 * 
	 * @param plainData
	 * @return
	 */
	public static String SM3Digest(String plainData) {

		SM3Digest sm3Digest = new SM3Digest();
		byte[] dataBytes = Utils.hex2Byte(plainData);

		byte[] t2Bytes = new byte[dataBytes.length];
		System.arraycopy(dataBytes, 0, t2Bytes, 0, dataBytes.length);

		sm3Digest.update(t2Bytes, 0, t2Bytes.length);
		byte[] hashBytes = new byte[32];
		sm3Digest.doFinal(hashBytes, 0);

		return Utils.bytes2Hex(hashBytes);

	}

	/****************************************************************/
	public static String encrypt(String publicKey, String data)
			throws IOException {
		// 国密规范正式公钥 04开头
		if (publicKey.length() == 128) {
			publicKey = "04" + publicKey;
		}
		return encrypt(Utils.hex2Byte(publicKey), data.getBytes());
	}

	// 数据加密
	public static String encrypt(byte[] publicKey, byte[] data)
			throws IOException {
		if (publicKey == null || publicKey.length == 0) {
			return null;
		}
		
		if(publicKey.length==64) {
			byte[] pubKeytmp = new byte[64+1];
			pubKeytmp[0] = 0x04;
			System.arraycopy(publicKey, 0, pubKeytmp, 1, publicKey.length);
			publicKey = pubKeytmp;
		}
		
		if (data == null || data.length == 0) {
			return null;
		}

		byte[] source = new byte[data.length];
		System.arraycopy(data, 0, source, 0, data.length);

		SM2Cipher cipher = new SM2Cipher();
		SM2 sm2 = new SM2();
		ECPoint userKey = sm2.eccCurve.decodePoint(publicKey);

		ECPoint c1 = cipher.Init_enc(sm2, userKey);
		cipher.Encrypt(source);
		byte[] c3 = new byte[32];
		cipher.Dofinal(c3);

		//System.out.println("C1 " + Utils.bytes2Hex(c1.getEncoded()));
		//System.out.println("C2 " + Utils.bytes2Hex(source));
		//System.out.println("C3 " + Utils.bytes2Hex(c3));
		// C1 C2 C3拼装成加密字串
		return Utils.bytes2Hex(c1.getEncoded(false)) + Utils.bytes2Hex(source)
				+ Utils.bytes2Hex(c3);

	}

	public static byte[] decrypt(String privateKey, String encryptedData)
			throws IOException {
		return decrypt(Utils.hex2Byte(privateKey),
				Utils.hex2Byte(encryptedData));
	}

	// 数据解密
	public static byte[] decrypt(byte[] privateKey, byte[] encryptedData)
			throws IOException {
		if (privateKey == null || privateKey.length == 0) {
			return null;
		}

		if (encryptedData == null || encryptedData.length == 0) {
			return null;
		}
		// 加密字节数组转换为十六进制的字符串 长度变为encryptedData.length * 2
		String data = Utils.bytes2Hex(encryptedData);
		/***
		 * 分解加密字串 （C1 = C1标志位2位 + C1实体部分128位 = 130） （C3 = C3实体部分64位 = 64） （C2 =
		 * encryptedData.length * 2 - C1长度 - C2长度）
		 */
		byte[] c1Bytes = Utils.hex2Byte(data.substring(0, 130));
		int c2Len = encryptedData.length - 97;
		byte[] c2 = Utils.hex2Byte(data.substring(130, 130 + 2 * c2Len));
		byte[] c3 = Utils.hex2Byte(data.substring(130 + 2 * c2Len,
				194 + 2 * c2Len));

		SM2 sm2 = new SM2();
		BigInteger userD = new BigInteger(1, privateKey);

		// 通过C1实体字节来生成ECPoint
		ECPoint c1 = sm2.eccCurve.decodePoint(c1Bytes);
		SM2Cipher cipher = new SM2Cipher();
		cipher.Init_dec(userD, c1);
		cipher.Decrypt(c2);
		//System.out.println("dec c3:" + Utils.bytes2Hex(c3));
		byte[] _c3 = new byte[32];
		cipher.Dofinal(_c3);
		
		// 计算 _c3 = Hash(x2 || M' || y2) 判断 _c3 == c3是否成立
		//System.out.println("dec _c3:" + Utils.bytes2Hex(_c3));
		if (Arrays.equals(_c3, c3)) {
			// 返回解密结果
			return c2;
		} else {
			// 解密失败
			throw new IOException(
					"IllegalArgumentException Invalid privateKey or encryptedData!");
		}
	}

	/****************************************************************/

	private static SecureRandom random = new SecureRandom();

	private static void printHexString(byte[] b) {
		System.out.println(Utils.bytes2Hex(b));
	}

	public static BigInteger random(BigInteger max) {
		BigInteger r = new BigInteger(256, random);
		// int count = 1;
		while (r.compareTo(max) >= 0) {
			r = new BigInteger(128, random);
			// count++;
		}
		// System.out.println("count: " + count);
		return r;
	}

	private static boolean allZero(byte[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] != 0)
				return false;
		}
		return true;
	}

	private static boolean between(BigInteger param, BigInteger min,
			BigInteger max) {
		if (param.compareTo(min) >= 0 && param.compareTo(max) < 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 公钥校验
	 * 
	 * @param publicKey
	 *            公钥
	 * @return boolean true或false
	 */
	private static boolean checkPublicKey(ECPoint publicKey) {
		SM2 sm2 = new SM2();

		// ECPublicKeyParameters ecPub = new ECPublicKeyParameters(publicKey,
		// sm2.eccBcSpec);

		if (!publicKey.isInfinity()) {
			BigInteger x = publicKey.getXCoord().toBigInteger();
			BigInteger y = publicKey.getYCoord().toBigInteger();
			if (between(x, new BigInteger("0"), sm2.eccP)
					&& between(y, new BigInteger("0"), sm2.eccP)) {
				BigInteger xResult = x.pow(3).add(sm2.eccA.multiply(x))
						.add(sm2.eccB).mod(sm2.eccP);
				//System.out.println("xResult: " + xResult.toString());
				BigInteger yResult = y.pow(2).mod(sm2.eccP);
				//System.out.println("yResult: " + yResult.toString());
				if (yResult.equals(xResult)
						&& publicKey.multiply(sm2.eccN).isInfinity()) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	private static byte[] calculateHash(BigInteger x2, byte[] M, BigInteger y2) {
		ShortenedDigest digest = new ShortenedDigest(new SHA256Digest(), 20);
		byte[] buf = x2.toByteArray();
		digest.update(buf, 0, buf.length);
		digest.update(M, 0, M.length);
		buf = y2.toByteArray();
		digest.update(buf, 0, buf.length);

		buf = new byte[20];
		digest.doFinal(buf, 0);
		return buf;
	}

	public static String encrypt2(String publicKey, String input) {
		SM2 sm2 = new SM2();
		BigInteger ax = new BigInteger(publicKey.substring(0,
				publicKey.length() / 2), 16);
		BigInteger ay = new BigInteger(
				publicKey.substring(publicKey.length() / 2), 16);

		ECPoint pointA = sm2.eccCurve.createPoint(ax, ay);

		return Utils.bytes2Hex(encrypt2(input, pointA));
	}

	/**
	 * 加密
	 * 
	 * @param input
	 *            待加密消息M
	 * @param publicKey
	 *            公钥
	 * @return byte[] 加密后的字节数组
	 */
	private static byte[] encrypt2(String input, ECPoint publicKey) {

		// System.out.println("publicKey is: "+publicKey);
		SM2 sm2 = new SM2();
		byte[] inputBuffer = input.getBytes();
		// printHexString(inputBuffer);

		/* 1 产生随机数k，k属于[1, n-1] */
		BigInteger k = random(new BigInteger(
				"7E893148E552929E4811F927F190C6E7FFBDCCF466351984F0736C74A7CFCC5E",
				16));
		// System.out.print("k: ");
		// printHexString(k.toByteArray());

		/* 2 计算椭圆曲线点C1 = [k]G = (x1, y1) */
		ECPoint C1 = sm2.eccPointG.multiply(k);
		byte[] C1Buffer = C1.getEncoded(false);
		// System.out.print("C1: ");
		// printHexString(C1Buffer);

		// 3 计算椭圆曲线点 S = [h]Pb * curve没有指定余因子，h为空

		// BigInteger h = curve.getCofactor(); System.out.print("h: ");
		// printHexString(h.toByteArray()); if (publicKey != null) { ECPoint
		// result = publicKey.multiply(h); if (!result.isInfinity()) {
		// System.out.println("pass"); } else {
		// System.err.println("计算椭圆曲线点 S = [h]Pb失败"); return null; } }

		/* 4 计算 [k]PB = (x2, y2) */
		ECPoint kpb = publicKey.multiply(k).normalize();

		/* 5 计算 t = KDF(x2||y2, klen) */
		byte[] kpbBytes = kpb.getEncoded(false);
		DerivationFunction kdf = new KDF1BytesGenerator(new ShortenedDigest(
				new SHA256Digest(), 20));
		byte[] t = new byte[inputBuffer.length];
		kdf.init(new ISO18033KDFParameters(kpbBytes));
		kdf.generateBytes(t, 0, t.length);

		if (allZero(t)) {
			System.err.println("all zero");
		}

		/* 6 计算C2=M^t */
		byte[] C2 = new byte[inputBuffer.length];
		for (int i = 0; i < inputBuffer.length; i++) {
			C2[i] = (byte) (inputBuffer[i] ^ t[i]);
		}

		/* 7 计算C3 = Hash(x2 || M || y2) */
		byte[] C3 = calculateHash(kpb.getXCoord().toBigInteger(), inputBuffer,
				kpb.getYCoord().toBigInteger());

		/* 8 输出密文 C=C1 || C2 || C3 */
		byte[] encryptResult = new byte[C1Buffer.length + C2.length + C3.length];
		System.arraycopy(C1Buffer, 0, encryptResult, 0, C1Buffer.length);
		System.arraycopy(C2, 0, encryptResult, C1Buffer.length, C2.length);
		System.arraycopy(C3, 0, encryptResult, C1Buffer.length + C2.length,
				C3.length);

		return encryptResult;
	}

	public static byte[] decrypt2(String privateKey, String encryptData)
			throws IOException {
		return decrypt2(new BigInteger(privateKey, 16),
				Utils.hex2Byte(encryptData));
	}

	public static byte[] decrypt2(BigInteger privateKey, byte[] encryptData)
			throws IOException {
		// System.out.println("privateKey is: "+privateKey);
		// System.out.println("encryptData length: " + encryptData.length);
		SM2 sm2 = new SM2();
		byte[] C1Byte = new byte[65];
		System.arraycopy(encryptData, 0, C1Byte, 0, C1Byte.length);

		ECPoint C1 = sm2.eccCurve.decodePoint(C1Byte).normalize();

		/* 计算[dB]C1 = (x2, y2) */
		ECPoint dBC1 = C1.multiply(privateKey).normalize();

		/* 计算t = KDF(x2 || y2, klen) */
		byte[] dBC1Bytes = dBC1.getEncoded(false);
		DerivationFunction kdf = new KDF1BytesGenerator(new ShortenedDigest(
				new SHA256Digest(), 20));

		int klen = encryptData.length - 65 - 20;
		// System.out.println("klen = " + klen);

		byte[] t = new byte[klen];
		kdf.init(new ISO18033KDFParameters(dBC1Bytes));
		kdf.generateBytes(t, 0, t.length);

		if (allZero(t)) {
			System.err.println("all zero");
		}

		/* 5 计算M'=C2^t */
		byte[] M = new byte[klen];
		for (int i = 0; i < M.length; i++) {
			M[i] = (byte) (encryptData[C1Byte.length + i] ^ t[i]);
		}

		/* 6 计算 u = Hash(x2 || M' || y2) 判断 u == C3是否成立 */
		byte[] C3 = new byte[20];
		System.arraycopy(encryptData, encryptData.length - 20, C3, 0, 20);
		byte[] u = calculateHash(dBC1.getXCoord().toBigInteger(), M, dBC1
				.getYCoord().toBigInteger());
		if (Arrays.equals(u, C3)) {
			// System.out.println("解密成功");
			// System.out.println("M' = " + new String(M));
			return M;
		} else {
			System.err.println("解密验证失败");
			System.out.print("u = ");
			printHexString(u);
			System.out.print("C3 = ");
			printHexString(C3);
			throw new IOException(
					"IllegalArgumentException Invalid privateKey or encryptedData!");
		}
	}

}