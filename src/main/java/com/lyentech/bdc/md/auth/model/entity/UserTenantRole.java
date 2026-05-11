package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-08-11
 */
@TableName("md_user_tenant_role")
public class UserTenantRole implements Serializable{

    private static final long serialVersionUID = -7549653328625519452L;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色id
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 租户id
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * appId
     */
    @TableField("app_id")
    private String appId;

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
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "UserTenantRole{" +
            "userId=" + userId +
            ", roleId=" + roleId +
            ", tenantId=" + tenantId +
        "}";
    }
}
