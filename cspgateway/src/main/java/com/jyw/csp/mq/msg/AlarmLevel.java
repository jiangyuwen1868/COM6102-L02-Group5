package com.jyw.csp.mq.msg;

public enum AlarmLevel {

	GENERAL("general", "一般"),
	WARN("warn", "警告"),
	SERIOUS("serious", "严重");
	
	private final String level;
    private final String info;
	
	AlarmLevel(String level, String info) {
		this.level = level;
		this.info = info;
	}

	public String getLevel() {
		return level;
	}

	public String getInfo() {
		return info;
	}
}
