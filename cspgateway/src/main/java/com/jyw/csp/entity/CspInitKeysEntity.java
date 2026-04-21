package com.jyw.csp.entity;

public class CspInitKeysEntity {
	
	private String keyid;
	private String key_alg_type;
	private String key_alg_code;
	private String key_checkvalue;
	private String key_ciphertextvalue;
	private String publickey;
	private String remark;
	public String getKeyid() {
		return keyid;
	}
	public void setKeyid(String keyid) {
		this.keyid = keyid;
	}
	public String getKey_alg_type() {
		return key_alg_type;
	}
	public void setKey_alg_type(String key_alg_type) {
		this.key_alg_type = key_alg_type;
	}
	public String getKey_alg_code() {
		return key_alg_code;
	}
	public void setKey_alg_code(String key_alg_code) {
		this.key_alg_code = key_alg_code;
	}
	public String getKey_checkvalue() {
		return key_checkvalue;
	}
	public void setKey_checkvalue(String key_checkvalue) {
		this.key_checkvalue = key_checkvalue;
	}
	public String getKey_ciphertextvalue() {
		return key_ciphertextvalue;
	}
	public void setKey_ciphertextvalue(String key_ciphertextvalue) {
		this.key_ciphertextvalue = key_ciphertextvalue;
	}
	public String getPublickey() {
		return publickey;
	}
	public void setPublickey(String publickey) {
		this.publickey = publickey;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

}
