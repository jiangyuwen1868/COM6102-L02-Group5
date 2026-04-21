/**
 * 
 */
package com.jyw.csp.util.chiper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.jyw.csp.util.Utils;

/**
 * 这是文档注释
 * @author 作者
 * @version 创建时间：2020年8月6日 下午9:12:42
 */
/**
 * @author Administrator
 * 
 */
public class SM4FileEncrypt {
	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			// No such provider: BC
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	// 文件解密用buffer长度
	private static int kBufferSize = 8192;

	// 生成 Cipher
	public static Cipher generateCipher(int mode, byte[] keyData)
			throws InvalidKeyException, NoSuchPaddingException,
			NoSuchAlgorithmException, NoSuchProviderException {
		Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding",
				BouncyCastleProvider.PROVIDER_NAME);
		Key sm4Key = new SecretKeySpec(keyData, "SM4");
		cipher.init(mode, sm4Key);
		return cipher;
	}

	// 加密文件
	public static void encryptFile(byte[] keyData, String sourcePath,
			String targetPath) {
		// 加密文件
		FileInputStream in = null;
		FileOutputStream fileout = null;
		CipherInputStream cipherin = null;
		try {
			Cipher cipher = generateCipher(Cipher.ENCRYPT_MODE, keyData);
			in = new FileInputStream(sourcePath);
			cipherin = new CipherInputStream(in, cipher);

			fileout = new FileOutputStream(targetPath);
			byte[] buffer = new byte[kBufferSize];
			int length;
			while ((length = cipherin.read(buffer)) != -1) {
				fileout.write(buffer, 0, length);
			}

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (cipherin != null) {
				try {
					cipherin.close();
				} catch (Exception e) {
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}

			if (fileout != null) {
				try {
					fileout.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 解密文件
	 * 
	 * @param sourcePath
	 *            待解密的文件路径
	 * @param targetPath
	 *            解密后的文件路径
	 */
	public static boolean decryptFile(byte[] keyData, String sourcePath,
			String targetPath) {

		FileInputStream in = null;

		FileOutputStream fileout = null;

		CipherInputStream cipherin = null;

		try {
			in = new FileInputStream(sourcePath);

			fileout = new FileOutputStream(targetPath);
			Cipher cipher = generateCipher(Cipher.DECRYPT_MODE, keyData);

			cipherin = new CipherInputStream(in, cipher);
			byte[] buffer = new byte[kBufferSize];
			int length;
			while ((length = cipherin.read(buffer)) != -1) {
				fileout.write(buffer, 0, length);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if (cipherin != null) {
				try {
					cipherin.close();
				} catch (Exception e) {
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}

			if (fileout != null) {
				try {
					fileout.close();
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param keyData
	 * @param sourcePath
	 * @return
	 */
	public static byte[] decryptFile(byte[] keyData, String sourcePath) {
		FileInputStream in = null;

		ByteArrayOutputStream bos = null;

		CipherInputStream cipherin = null;
		try {
			in = new FileInputStream(sourcePath);

			bos = new ByteArrayOutputStream(in.available());
			byte[] buffer = new byte[in.available()];
			Cipher cipher = generateCipher(Cipher.DECRYPT_MODE, keyData);

			cipherin = new CipherInputStream(in, cipher);
			int length;
			while ((length = cipherin.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if (cipherin != null) {
				try {
					cipherin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	// 计算文件摘要
	public static String fileDigest(String fileName) {
//		Digest digest = new SM3Digest();
//		byte[] hv = new byte[digest.getDigestSize()];

		FileInputStream fin = null;
		byte[] hashResult = null;
		try {
			MessageDigest hash = MessageDigest.getInstance("SM3");
			fin = new FileInputStream(fileName);
			int length;
			// fin.skip(24);
			byte[] buffer = new byte[kBufferSize];
			while ((length = fin.read(buffer)) != -1) {
				hash.update(buffer, 0, length);
			}
//			digest.update(buffer,0,length);
//			digest.doFinal(hv, 0);

			hashResult = hash.digest();

			return Utils.bytes2Hex(hashResult);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
