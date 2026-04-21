package com.jyw.csp.entity;

import java.io.Serializable;
import java.util.Date;

public class CspHsmGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long hsmGroupId;
    private String hsmGroupName;
    private String hsmGroupCategory;
    private String hsmGroupDesc;
    private int connectTimeout;
    private int connectPoolSize;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;

    public Long getHsmGroupId() {
        return hsmGroupId;
    }

    public void setHsmGroupId(Long hsmGroupId) {
        this.hsmGroupId = hsmGroupId;
    }

    public String getHsmGroupName() {
        return hsmGroupName;
    }

    public void setHsmGroupName(String hsmGroupName) {
        this.hsmGroupName = hsmGroupName;
    }

    public String getHsmGroupCategory() {
        return hsmGroupCategory;
    }

    public void setHsmGroupCategory(String hsmGroupCategory) {
        this.hsmGroupCategory = hsmGroupCategory;
    }

    public String getHsmGroupDesc() {
        return hsmGroupDesc;
    }

    public void setHsmGroupDesc(String hsmGroupDesc) {
        this.hsmGroupDesc = hsmGroupDesc;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectPoolSize() {
        return connectPoolSize;
    }

    public void setConnectPoolSize(int connectPoolSize) {
        this.connectPoolSize = connectPoolSize;
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

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
