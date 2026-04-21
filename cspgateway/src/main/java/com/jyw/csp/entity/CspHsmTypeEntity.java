package com.jyw.csp.entity;

import java.io.Serializable;
import java.util.Date;

public class CspHsmTypeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long hsmTypeId;
    private String hsmType;
    private String hsmCategory;
    private String hsmVendor;
    private String tsName;
    private String tsMobile;
    private String tsEmail;
    private String remark;
    private String creator;
    private Date createTime;

    public Long getHsmTypeId() {
        return hsmTypeId;
    }

    public void setHsmTypeId(Long hsmTypeId) {
        this.hsmTypeId = hsmTypeId;
    }

    public String getHsmType() {
        return hsmType;
    }

    public void setHsmType(String hsmType) {
        this.hsmType = hsmType;
    }

    public String getHsmCategory() {
		return hsmCategory;
	}

	public void setHsmCategory(String hsmCategory) {
		this.hsmCategory = hsmCategory;
	}

	public String getHsmVendor() {
        return hsmVendor;
    }

    public void setHsmVendor(String hsmVendor) {
        this.hsmVendor = hsmVendor;
    }

    public String getTsName() {
        return tsName;
    }

    public void setTsName(String tsName) {
        this.tsName = tsName;
    }

    public String getTsMobile() {
        return tsMobile;
    }

    public void setTsMobile(String tsMobile) {
        this.tsMobile = tsMobile;
    }

    public String getTsEmail() {
        return tsEmail;
    }

    public void setTsEmail(String tsEmail) {
        this.tsEmail = tsEmail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
