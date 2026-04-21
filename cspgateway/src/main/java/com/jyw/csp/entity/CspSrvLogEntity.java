package com.jyw.csp.entity;

import java.util.Date;

public class CspSrvLogEntity {

	private String logid;
	private String appid;
	private String nodeid;
	private String branchid;
	private String trace_id;
	private Date recv_time;
	private Date resp_time;
	private String txcode;
	private String transret;
	private String hostname;
	private String hostip;
	private String errorcode;
	private String innererrcode;
	private String errormsg;
	private String reqmsg;
	private String rspmsg;
	private String costtimeinfo;
	private String signature;
	public String getLogid() {
		return logid;
	}
	public void setLogid(String logid) {
		this.logid = logid;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getNodeid() {
		return nodeid;
	}
	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}
	public String getBranchid() {
		return branchid;
	}
	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}
	public String getTrace_id() {
		return trace_id;
	}
	public void setTrace_id(String trace_id) {
		this.trace_id = trace_id;
	}
	public Date getRecv_time() {
		return recv_time;
	}
	public void setRecv_time(Date recv_time) {
		this.recv_time = recv_time;
	}
	public Date getResp_time() {
		return resp_time;
	}
	public void setResp_time(Date resp_time) {
		this.resp_time = resp_time;
	}
	public String getTxcode() {
		return txcode;
	}
	public void setTxcode(String txcode) {
		this.txcode = txcode;
	}
	public String getTransret() {
		return transret;
	}
	public void setTransret(String transret) {
		this.transret = transret;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getHostip() {
		return hostip;
	}
	public void setHostip(String hostip) {
		this.hostip = hostip;
	}
	public String getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}
	public String getInnererrcode() {
		return innererrcode;
	}
	public void setInnererrcode(String innererrcode) {
		this.innererrcode = innererrcode;
	}
	public String getErrormsg() {
		return errormsg;
	}
	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}
	public String getReqmsg() {
		return reqmsg;
	}
	public void setReqmsg(String reqmsg) {
		this.reqmsg = reqmsg;
	}
	public String getRspmsg() {
		return rspmsg;
	}
	public void setRspmsg(String rspmsg) {
		this.rspmsg = rspmsg;
	}
	public String getCosttimeinfo() {
		return costtimeinfo;
	}
	public void setCosttimeinfo(String costtimeinfo) {
		this.costtimeinfo = costtimeinfo;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String toSignString() {
		StringBuffer sb = new StringBuffer();
		sb.append(logid);
		sb.append("|");
		sb.append(appid);
		sb.append("|");
		sb.append(nodeid);
		sb.append("|");
		sb.append(branchid);
		sb.append("|");
		sb.append(trace_id);
		sb.append("|");
		sb.append(recv_time);
		sb.append("|");
		sb.append(resp_time);
		sb.append("|");
		sb.append(txcode);
		sb.append("|");
		sb.append(transret);
		sb.append("|");
		sb.append(hostname);
		sb.append("|");
		sb.append(hostip);
		sb.append("|");
		sb.append(errorcode);
		sb.append("|");
		sb.append(innererrcode);
		sb.append("|");
		sb.append(errormsg);
		sb.append("|");
		sb.append(reqmsg);
		sb.append("|");
		sb.append(rspmsg);
		sb.append("|");
		sb.append(costtimeinfo);
		return sb.toString();
	}
}
