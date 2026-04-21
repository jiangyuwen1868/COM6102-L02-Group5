package com.jyw.csp.util.chiper;

import java.nio.charset.Charset;

import com.jyw.csp.util.Base64;


public class XORCipher {

	private static final String secrectKey = "Anydef20151010智能身份识别平台FECOI{%biap%}()*&<MNCXZPKL";
	private static final Charset charset = Charset.forName("UTF-8");
	private static byte[] keyBytes = secrectKey.getBytes(charset);

	public static String encode(String enc) {
		byte[] b = enc.getBytes(charset);
		for (int i = 0, size = b.length; i < size; i++) {
			for (byte keyBytes0 : keyBytes) {
				b[i] = (byte) (b[i] ^ keyBytes0);
			}
		}
		return new String(Base64.encode(b));
	}

	public static String decode(String dec) {
		byte[] e = Base64.decode(dec.getBytes());
		byte[] dee = e;
		for (int i = 0, size = e.length; i < size; i++) {
			for (byte keyBytes0 : keyBytes) {
				e[i] = (byte) (dee[i] ^ keyBytes0);
			}
		}
		return new String(e, charset);
	}

	public static void main(String[] args) {
		String s = "安御道合you are right！123FGF!@#$%中国232。";
		int count = 1;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			String enc = encode(s);
			String dec = decode(enc);
			System.out.println("enc:" + enc);
			System.out.println("dec:" + dec);
		}
		long end = System.currentTimeMillis();
		long costTime = end - start;
		System.out.println("costTime:" + costTime + "ms. avg:" + costTime / (float)count + "ms.");
	}
}
