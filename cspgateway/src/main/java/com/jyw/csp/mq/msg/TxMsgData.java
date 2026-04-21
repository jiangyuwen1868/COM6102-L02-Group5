package com.jyw.csp.mq.msg;

public class TxMsgData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1149879324179701671L;
	private String txcode;
	private String appid;
	private String status;
	private String branchid;
	public String getTxcode() {
		return txcode;
	}
	public void setTxcode(String txcode) {
		this.txcode = txcode;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}
}
