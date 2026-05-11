package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * @author guolanren
 */
@TableName(value = "md_user_app_role")
public class MdUserAppRole implements Serializable {

    private static final long serialVersionUID = 6310195629772197031L;

    @TableField("app_id")
    private String appId;

    @TableField("user_id")
    private Long userId;

    @TableField("role_id")
    private Long roleId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MdUserRole{");
        sb.append("appId='").append(appId).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", roleId='").append(roleId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
