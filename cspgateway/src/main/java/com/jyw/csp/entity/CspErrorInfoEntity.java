package com.jyw.csp.entity;

public class CspErrorInfoEntity {

	private String errorcode;
	private String errortype;
	private String errormsg;
	private String isconv;
	private String convertmsg;
	private String remark;
	public String getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}
	public String getErrortype() {
		return errortype;
	}
	public void setErrortype(String errortype) {
		this.errortype = errortype;
	}
	public String getErrormsg() {
		return errormsg;
	}
	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}
	public String getIsconv() {
		return isconv;
	}
	public void setIsconv(String isconv) {
		this.isconv = isconv;
	}
	public String getConvertmsg() {
		return convertmsg;
	}
	public void setConvertmsg(String convertmsg) {
		this.convertmsg = convertmsg;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
