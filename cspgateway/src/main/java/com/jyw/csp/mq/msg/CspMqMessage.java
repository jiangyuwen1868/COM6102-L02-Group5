package com.jyw.csp.mq.msg;

import java.util.Date;

public class CspMqMessage implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3789877461359672680L;
	
	private String msgId;
	private String msgType;
	private String msgData;
	private Date createTime;
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getMsgData() {
		return msgData;
	}
	public void setMsgData(String msgData) {
		this.msgData = msgData;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
