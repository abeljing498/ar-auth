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
@TableName("md_app_tenant")
public class AppTenant implements Serializable{

    private static final long serialVersionUID = -1847444768516123465L;

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

    @TableField("auth_app")
    private Integer authApp;

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

    public Integer getAuthApp() {
        return authApp;
    }

    public void setAuthApp(Integer authApp) {
        this.authApp = authApp;
    }

    @Override
    public String toString() {
        return "AppTenant{" +
                "appId='" + appId + '\'' +
                ", tenantId=" + tenantId +
                ", authApp=" + authApp +
                '}';
    }
}
