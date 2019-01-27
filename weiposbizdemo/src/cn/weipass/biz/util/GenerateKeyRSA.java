package cn.weipass.biz.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class GenerateKeyRSA {
	private KeyPairGenerator kpg = null;

	private KeyPair kp = null;

	private PublicKey public_key = null;

	private PrivateKey private_key = null;

	/**
	 * 构造函数
	 * 
	 * @param in
	 *            指定密匙长度（取值范围：512～2048）
	 * @throws NoSuchAlgorithmException
	 *             异常
	 */
	public GenerateKeyRSA(int in) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		kpg = KeyPairGenerator.getInstance("RSA"); // 创建‘密匙对’生成器
		kpg.initialize(in); // 指定密匙长度（取值范围：512～2048）
		kp = kpg.genKeyPair(); // 生成‘密匙对’，其中包含着一个公匙和一个私匙的信息
		public_key = kp.getPublic(); // 获得公匙
		private_key = kp.getPrivate(); // 获得私匙
	}

	/**
	 * 获得加密密钥
	 * 
	 * @return
	 */
	public PrivateKey getPrivateKey() {
		return this.private_key;
	}

	/**
	 * 获得加密公钥
	 * 
	 * @return
	 */
	public PublicKey getPublicKey() {
		return this.public_key;
	}

	/**
	 * 获得密钥对
	 * 
	 * @return
	 */
	public KeyPair getKeyPair() {
		return this.kp;
	}


	public final static byte[] rsaEncryptNoPadding_public(byte[] data, byte[] key) {
		Cipher cipher = null;
		try {
			KeySpec keySpec = new X509EncodedKeySpec(key);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey k = factory.generatePublic(keySpec);
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, k);
			data = cipher.doFinal(data);
			return data;
		} catch (Throwable t) {
			t.printStackTrace();
			// System.out.println("加密失败");
			return null;
		}
	}

	public final static byte[] rsaEncryptNoPadding_private(byte[] data, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//			RSAPrivateKey k = RSAPrivateCrtKeyImpl.newKey(key);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKey k = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
			
			cipher.init(Cipher.ENCRYPT_MODE, k);
			data = cipher.doFinal(data);
			return data;
		} catch (Throwable t) {
			t.printStackTrace();
			// System.out.println("加密失败");
			return null;
		}
	}
	

	/**
	 * 平台私钥解密
	 * @param data
	 * @param key
	 * @return
	 */
	public final static byte[] rsaDecryptNoPadding_private(byte[] data, byte[] key) {
		int datasize = data.length;
		int keysize = key.length;
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//			cipher = Cipher.getInstance("RSA");
//			RSAPrivateKey k = RSAPrivateCrtKeyImpl.newKey(key);
			
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKey k = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
			
			//类型解密模式
			cipher.init(Cipher.DECRYPT_MODE, k);
//			data = cipher.doFinal(data);
//			return data;
			byte[] datas = cipher.doFinal(data);
			return datas;
		} catch (Throwable t) {
			t.printStackTrace();
			String msg = t.getMessage();
			// System.out.println("解密失败");
			return null;
		}
	}
	/**
	 * rsa 平台公钥解密
	 * @param data
	 * @param key
	 * @return
	 */
	public final static byte[] rsaDecryptNoPadding_public(byte[] data, byte[] key) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA");
//			cipher = Cipher.getInstance("RSA");
			
			KeySpec keySpec = new X509EncodedKeySpec(key);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey k = factory.generatePublic(keySpec);
			
			cipher.init(Cipher.DECRYPT_MODE, k);
			data = cipher.doFinal(data);
			return data;
		} catch (Throwable t) {
			t.printStackTrace();
			String msg = t.getMessage();
			// System.out.println("解密失败");
			return null;
		}
	}
	
	
	

}
