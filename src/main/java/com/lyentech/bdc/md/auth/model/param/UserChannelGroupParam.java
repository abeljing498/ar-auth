package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */


public class UserChannelGroupParam implements Serializable {
    private Integer id;

    private String name;

    private String appId;
    private Long tenantId;
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    private String customId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }
}
