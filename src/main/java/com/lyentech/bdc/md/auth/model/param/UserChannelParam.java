package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */


public class UserChannelParam implements Serializable {

    private Integer id;

    private Integer groupId;

    private String name;

    private String appId;
    private String customId;
    private Long tenantId;
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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


}
