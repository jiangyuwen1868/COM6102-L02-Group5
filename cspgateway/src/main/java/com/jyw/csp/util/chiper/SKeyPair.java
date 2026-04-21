package com.jyw.csp.util.chiper;

public class SKeyPair {
	private String privateKey;
	private String publicKey;
	
	public SKeyPair(String privateKey, String publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
