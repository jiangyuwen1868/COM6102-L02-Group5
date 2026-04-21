package com.jyw.csp.entity;

public class CspFlowControlEntity {

	private String appid;
	private String txcode;
	private Integer tpscount;
	private String opswitch;
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getTxcode() {
		return txcode;
	}
	public void setTxcode(String txcode) {
		this.txcode = txcode;
	}
	public Integer getTpscount() {
		return tpscount;
	}
	public void setTpscount(Integer tpscount) {
		this.tpscount = tpscount;
	}
	public String getOpswitch() {
		return opswitch;
	}
	public void setOpswitch(String opswitch) {
		this.opswitch = opswitch;
	}
}
