package com.jyw.csp.datatransform.message.gw;

import com.alibaba.fastjson.annotation.JSONField;
import com.jyw.csp.datatransform.message.tx.TxRequestMsg;

public class GwRequestMsg {

	private String app_id;
	private String signature;
	private String sec_version;
	private String request_info;
	
	@JSONField(serialize =false)
	private TxRequestMsg txRequestMsg;
	@JSONField(serialize =false)
	private String plaintextRequestInfo;
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getSec_version() {
		return sec_version;
	}
	public void setSec_version(String sec_version) {
		this.sec_version = sec_version;
	}
	public String getRequest_info() {
		return request_info;
	}
	public void setRequest_info(String request_info) {
		this.request_info = request_info;
	}
	public TxRequestMsg getTxRequestMsg() {
		return txRequestMsg;
	}
	public void setTxRequestMsg(TxRequestMsg txRequestMsg) {
		this.txRequestMsg = txRequestMsg;
	}
	public String getPlaintextRequestInfo() {
		return plaintextRequestInfo;
	}
	public void setPlaintextRequestInfo(String plaintextRequestInfo) {
		this.plaintextRequestInfo = plaintextRequestInfo;
	}
}
