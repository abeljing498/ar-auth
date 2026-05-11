package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */

@TableName("md_app_sso")
public class AppSso implements Serializable {

    private static final long serialVersionUID = -1625587297449992237L;

    @TableField("app_id")
    private String appId;

    @TableField("sso_app_id")
    private String ssoAppId;

    @TableField("sso_app_key")
    private String ssoAppKey;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSsoAppId() {
        return ssoAppId;
    }

    public void setSsoAppId(String ssoAppId) {
        this.ssoAppId = ssoAppId;
    }

    public String getSsoAppKey() {
        return ssoAppKey;
    }

    public void setSsoAppKey(String ssoAppKey) {
        this.ssoAppKey = ssoAppKey;
    }
}
