package com.jyw.csp.entity;

import java.util.Date;

/**
 * 
 *应用信息表实体Entity
 */
public class CspAppInfoEntity {

	private String appid;
	private String appname;
	private String appsecret;
	private String tenantid;
	private String authtype;
	private String appstatus;
	private String hsmgroups;
	private String creator;
	private Date creattime;
	private String appdesc;
	private String srvgroups;
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getAppsecret() {
		return appsecret;
	}
	public void setAppsecret(String appsecret) {
		this.appsecret = appsecret;
	}
	public String getTenantid() {
		return tenantid;
	}
	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}
	public String getAuthtype() {
		return authtype;
	}
	public void setAuthtype(String authtype) {
		this.authtype = authtype;
	}
	public String getAppstatus() {
		return appstatus;
	}
	public void setAppstatus(String appstatus) {
		this.appstatus = appstatus;
	}
	public String getHsmgroups() {
		return hsmgroups;
	}
	public void setHsmgroups(String hsmgroups) {
		this.hsmgroups = hsmgroups;
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
	public String getAppdesc() {
		return appdesc;
	}
	public void setAppdesc(String appdesc) {
		this.appdesc = appdesc;
	}
	public String getSrvgroups() {
		return srvgroups;
	}
	public void setSrvgroups(String srvgroups) {
		this.srvgroups = srvgroups;
	}
}
