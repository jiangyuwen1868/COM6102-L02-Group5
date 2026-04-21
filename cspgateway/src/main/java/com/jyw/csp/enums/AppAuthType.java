package com.jyw.csp.enums;

public enum AppAuthType {

	AUTH01("01", "IP白名单"),
	AUTH02("02", "IP白名单+签名"),
	AUTH03("03", "加密+签名"),
	AUTH04("04", "IP白名单+加密+签名"),
	AUTH05("05", "加密"),
	AUTH06("06", "签名"),
	AUTH07("07", "无认证");
	
	private final String code;
    private final String info;

    AppAuthType(String code, String info) {
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
