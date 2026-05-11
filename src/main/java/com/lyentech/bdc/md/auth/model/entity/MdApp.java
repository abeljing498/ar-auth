package com.lyentech.bdc.md.auth.model.entity;

import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.io.Serializable;

/**
 * @author guolanren
 */
public class MdApp extends BaseClientDetails implements Serializable {

    private static final long serialVersionUID = -882241718451636838L;

    private String homepageUrl;

    private Integer loginKick;

    private String appRemark;
//
//    private String appAclInfoMd5;
//
//    public String getAppAclInfoMd5() {
//        return appAclInfoMd5;
//    }
//
//    public void setAppAclInfoMd5(String appAclInfoMd5) {
//        this.appAclInfoMd5 = appAclInfoMd5;
//    }

    public Integer getLoginKick() {
        return loginKick;
    }

    public void setLoginKick(Integer loginKick) {
        this.loginKick = loginKick;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

}
