package com.jyw.csp.mq.msg;

public enum MqMessageType {

	TYPE01("01", "交易通知"),
	TYPE02("02", "告警通知");
	
	private final String type;
    private final String info;
    
    MqMessageType(String type, String info) {
    	this.type = type;
    	this.info = info;
    }

	public String getType() {
		return type;
	}

	public String getInfo() {
		return info;
	}
}
