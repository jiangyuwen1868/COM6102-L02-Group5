package com.jyw.csp.entity;

import java.util.Date;

public class CspAppIpWhitelistEntity {

	private String appid;
	private String branchid;
	private String ip;
	private String ipdesc;
	private String creator;
	private Date creattime;
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getBranchid() {
		return branchid;
	}
	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getIpdesc() {
		return ipdesc;
	}
	public void setIpdesc(String ipdesc) {
		this.ipdesc = ipdesc;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Date getCreattime() {
		return creattime;
	}
	public void setCreattime(Date creattime) {
		this.creattime = creattime;
	}
}
