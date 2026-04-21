package com.jyw.csp.entity;

public class CspSysDeployEntity {

	private String groupid;
	private String deployid;
	private String hostname;
	private String username;
	private String deploycode;
	private String appcode;
	private String sysip;
	private int sysport;
	private String servaddr;
	private String isuseful;
	private int weight;
	private String remark;
	public String getGroupid() {
		return groupid;
	}
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}
	public String getDeployid() {
		return deployid;
	}
	public void setDeployid(String deployid) {
		this.deployid = deployid;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDeploycode() {
		return deploycode;
	}
	public void setDeploycode(String deploycode) {
		this.deploycode = deploycode;
	}
	public String getAppcode() {
		return appcode;
	}
	public void setAppcode(String appcode) {
		this.appcode = appcode;
	}
	public String getSysip() {
		return sysip;
	}
	public void setSysip(String sysip) {
		this.sysip = sysip;
	}
	public int getSysport() {
		return sysport;
	}
	public void setSysport(int sysport) {
		this.sysport = sysport;
	}
	public String getServaddr() {
		return servaddr;
	}
	public void setServaddr(String servaddr) {
		this.servaddr = servaddr;
	}
	public String getIsuseful() {
		return isuseful;
	}
	public void setIsuseful(String isuseful) {
		this.isuseful = isuseful;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
