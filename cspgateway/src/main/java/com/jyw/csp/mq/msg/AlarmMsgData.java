package com.jyw.csp.mq.msg;

import com.jyw.csp.util.string.StringUtils;

public class AlarmMsgData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2346551835506114840L;
	private static int ALARM_FIELD_MAX_LENGTH = 255;
	private String alarmContent;
	private String alarmTime;
	private String alarmLevel;
	private String alarmType;
	private String alarmHostIp;
	private String alarmHostDesc;
	private String alarmDesc;
	public String getAlarmContent() {
		return alarmContent;
	}
	public void setAlarmContent(String alarmContent) {
		this.alarmContent = alarmContent;
	}
	public String getAlarmTime() {
		return alarmTime;
	}
	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}
	public String getAlarmLevel() {
		return alarmLevel;
	}
	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getAlarmHostIp() {
		return alarmHostIp;
	}
	public void setAlarmHostIp(String alarmHostIp) {
		this.alarmHostIp = alarmHostIp;
	}
	public String getAlarmHostDesc() {
		return alarmHostDesc;
	}
	public void setAlarmHostDesc(String alarmHostDesc) {
		this.alarmHostDesc = alarmHostDesc;
	}
	public String getAlarmDesc() {
		return alarmDesc;
	}
	public void setAlarmDesc(String alarmDesc) {
		if(!StringUtils.isEmpty(alarmDesc) && alarmDesc.length()>ALARM_FIELD_MAX_LENGTH) {
			alarmDesc.substring(0, ALARM_FIELD_MAX_LENGTH);
		}
		this.alarmDesc = alarmDesc;
	}
}
