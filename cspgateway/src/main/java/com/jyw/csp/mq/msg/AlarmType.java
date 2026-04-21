package com.jyw.csp.mq.msg;

public enum AlarmType {

	NET("01", "网络"),
	DB("02", "数据库"),
	HSM("03", "加密机"),
	REDIS("04", "Redis"),
	GATEWAY("05", "接入网关服务"),
	SERVICE("06", "密码服务"),
	OTHER("07", "其他");
	
	private final String type;
    private final String info;
    
    AlarmType(String type, String info) {
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
