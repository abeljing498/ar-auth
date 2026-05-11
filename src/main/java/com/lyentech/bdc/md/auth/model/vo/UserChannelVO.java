package com.lyentech.bdc.md.auth.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;

import java.util.List;

/**
 * @author 260442
 */
public class UserChannelVO {
    private Integer id;
    private String name;
    private String appId;
    private String customId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    private Long tenantId;
    private List<UserChannel> userChannelList;

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

    public List<UserChannel> getUserChannelList() {
        return userChannelList;
    }

    public void setUserChannelList(List<UserChannel> userChannelList) {
        this.userChannelList = userChannelList;
    }


}
