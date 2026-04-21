package com.jyw.csp.entity;

public class CspResourceGroupEntity {

	private String groupid;
	private String reqmethod;
	private String contenttype;
	private int conntimeout;
	private int sotimeout;
	private int connmaxsize;
	private int heartinteval;
	private String lbstrategy;
	private String isbalance;
	private String isuseproxyauthor;
	private String proxyip;
	private int proxyport;
	private String proxyusername;
	private String proxyuserpass;
	private String remark;
	public String getGroupid() {
		return groupid;
	}
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}
	public String getReqmethod() {
		return reqmethod;
	}
	public void setReqmethod(String reqmethod) {
		this.reqmethod = reqmethod;
	}
	public String getContenttype() {
		return contenttype;
	}
	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}
	public int getConntimeout() {
		return conntimeout;
	}
	public void setConntimeout(int conntimeout) {
		this.conntimeout = conntimeout;
	}
	public int getSotimeout() {
		return sotimeout;
	}
	public void setSotimeout(int sotimeout) {
		this.sotimeout = sotimeout;
	}
	public int getConnmaxsize() {
		return connmaxsize;
	}
	public void setConnmaxsize(int connmaxsize) {
		this.connmaxsize = connmaxsize;
	}
	public int getHeartinteval() {
		return heartinteval;
	}
	public void setHeartinteval(int heartinteval) {
		this.heartinteval = heartinteval;
	}
	public String getLbstrategy() {
		return lbstrategy;
	}
	public void setLbstrategy(String lbstrategy) {
		this.lbstrategy = lbstrategy;
	}
	public String getIsbalance() {
		return isbalance;
	}
	public void setIsbalance(String isbalance) {
		this.isbalance = isbalance;
	}
	public String getIsuseproxyauthor() {
		return isuseproxyauthor;
	}
	public void setIsuseproxyauthor(String isuseproxyauthor) {
		this.isuseproxyauthor = isuseproxyauthor;
	}
	public String getProxyip() {
		return proxyip;
	}
	public void setProxyip(String proxyip) {
		this.proxyip = proxyip;
	}
	public int getProxyport() {
		return proxyport;
	}
	public void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}
	public String getProxyusername() {
		return proxyusername;
	}
	public void setProxyusername(String proxyusername) {
		this.proxyusername = proxyusername;
	}
	public String getProxyuserpass() {
		return proxyuserpass;
	}
	public void setProxyuserpass(String proxyuserpass) {
		this.proxyuserpass = proxyuserpass;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
