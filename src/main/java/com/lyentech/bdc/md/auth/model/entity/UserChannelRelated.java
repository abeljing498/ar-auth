package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */

@TableName("md_user_channel_related")
public class UserChannelRelated implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("user_id")
    private Long userId;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("group_id")
    private Long groupId;
    @TableField("channel_id")
    private Long channelId;
    @TableField("app_id")
    private String appId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value ="update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    public Long getTenantId() {
        return tenantId;
    }
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
