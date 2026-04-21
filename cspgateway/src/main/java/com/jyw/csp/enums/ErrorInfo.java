package com.jyw.csp.enums;

/**
 * 错误信息
 */
public enum ErrorInfo {
    SUCCESS("000000000000", "成功"),
    UNKNOWN("XAN999999999", "未知错误"),
    REQUEST_EMPTY("XMS100010001", "请求报文不能为空"), 
    REQUEST_INCORRECT("XMS100010002", "请求报文不正确"), 
    REQUEST_PARSE_ERROR("XMS100010003", "请求报文解析错误"),
	DB_ERROR("XDB999999999", "系统错误，请稍后再试");

    private final String code;
    private final String info;

    ErrorInfo(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
