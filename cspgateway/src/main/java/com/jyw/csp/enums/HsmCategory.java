package com.jyw.csp.enums;

public enum HsmCategory {
    CATEGORY01("01", "金融密码机"),
    CATEGORY02("02", "服务器密码机"),
    CATEGORY03("03", "签名验签服务器"),
    CATEGORY04("04", "时间戳服务器"),
    CATEGORY05("05", "电子签章服务器");

    private final String code;
    private final String info;

    HsmCategory(String code, String info) {
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
