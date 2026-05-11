package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@TableName("md_tenant_role")
public class TenantRole implements Serializable{

    private static final long serialVersionUID = -2036870835764382543L;

    /**
     * appId
     */
    @TableField("app_id")
    private String appId;

    /**
     * 租户id
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 角色id
     */
    @TableField("role_id")
    private Long roleId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "TenantRole{" +
            "appId=" + appId +
            ", tenantId=" + tenantId +
            ", roleId=" + roleId +
        "}";
    }
}
