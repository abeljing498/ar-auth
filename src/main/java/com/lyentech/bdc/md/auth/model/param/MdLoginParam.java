package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;

/**
 * @author guolanren
 */
public class MdLoginParam implements Serializable {

    private static final long serialVersionUID = -4987166591569088206L;

    private String phone;
    private String password;
    private String email;
    private String smsCode;
    private String authType;
    private String returnTo;
    private String callback;
    private String account;
    private String loginSystem;
    private String loginBrowser;
    private Long tenantId;
    private String credential;
    private String wechatCode;

    public String getWechatCode() {
        return wechatCode;
    }

    public void setWechatCode(String wechatCode) {
        this.wechatCode = wechatCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(String returnTo) {
        this.returnTo = returnTo;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    public String getLoginBrowser() {
        return loginBrowser;
    }

    public void setLoginBrowser(String loginBrowser) {
        this.loginBrowser = loginBrowser;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

}
