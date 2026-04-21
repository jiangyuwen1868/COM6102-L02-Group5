package com.jyw.csp.entity;

import java.io.Serializable;
import java.util.Date;

public class CspHsmInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long hsmId;
    private Long hsmTypeId;
    private String hsmName;
    private String hsmSn;
    private String hsmIp;
    private Integer hsmPort;
    private String hsmPwd;
    private String hsmContextPath;
    private Long hsmGroupId;
    private String hsmStatus;
    private boolean keepAlive;
    private boolean enable;
    private String remark;
    private String creator;
    private Date createTime;
    private CspHsmTypeEntity hsmType;
    private CspHsmGroupEntity hsmGroup;

    public Long getHsmId() {
        return hsmId;
    }

    public void setHsmId(Long hsmId) {
        this.hsmId = hsmId;
    }

    public Long getHsmTypeId() {
        return hsmTypeId;
    }

    public void setHsmTypeId(Long hsmTypeId) {
        this.hsmTypeId = hsmTypeId;
    }

    public String getHsmName() {
        return hsmName;
    }

    public void setHsmName(String hsmName) {
        this.hsmName = hsmName;
    }

    public String getHsmSn() {
        return hsmSn;
    }

    public void setHsmSn(String hsmSn) {
        this.hsmSn = hsmSn;
    }

    public String getHsmIp() {
        return hsmIp;
    }

    public void setHsmIp(String hsmIp) {
        this.hsmIp = hsmIp;
    }

    public Integer getHsmPort() {
        return hsmPort;
    }

    public void setHsmPort(Integer hsmPort) {
        this.hsmPort = hsmPort;
    }

    public String getHsmPwd() {
        return hsmPwd;
    }

    public void setHsmPwd(String hsmPwd) {
        this.hsmPwd = hsmPwd;
    }

    public String getHsmContextPath() {
		return hsmContextPath;
	}

	public void setHsmContextPath(String hsmContextPath) {
		this.hsmContextPath = hsmContextPath;
	}

	public Long getHsmGroupId() {
        return hsmGroupId;
    }

    public void setHsmGroupId(Long hsmGroupId) {
        this.hsmGroupId = hsmGroupId;
    }

    public String getHsmStatus() {
        return hsmStatus;
    }

    public void setHsmStatus(String hsmStatus) {
        this.hsmStatus = hsmStatus;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public CspHsmTypeEntity getHsmType() {
        return hsmType;
    }

    public void setHsmType(CspHsmTypeEntity hsmType) {
        this.hsmType = hsmType;
    }

    public CspHsmGroupEntity getHsmGroup() {
        return hsmGroup;
    }

    public void setHsmGroup(CspHsmGroupEntity hsmGroup) {
        this.hsmGroup = hsmGroup;
    }
}
