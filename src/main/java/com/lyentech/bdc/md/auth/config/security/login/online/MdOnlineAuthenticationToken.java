package com.lyentech.bdc.md.auth.config.security.login.online;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class MdOnlineAuthenticationToken extends AbstractAuthenticationToken {
    private String code;
    private String clientId;
    private String loginSystem;
    private String loginBrowser;
    private String userIp;
    private Long tenantId;
    public MdOnlineAuthenticationToken(String code, String clientId, String loginSystem, String loginBrowser, String userIp, Long tenantId) {
        super(null);
        this.code = code;
        this.clientId = clientId;
        this.loginBrowser=loginBrowser;
        this.loginSystem=loginSystem;
        this.userIp=userIp;
        this.tenantId=tenantId;
    }


    public String getCode() {
        return code;
    }

    public Long getTenantId() {
        return tenantId;
    }
    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
         return code;
    }

    public String getClientId() {
        return clientId;
    }

    public String getLoginSystem() {
        return loginSystem;
    }

    public String getLoginBrowser() {
        return loginBrowser;
    }

    public String getUserIp() {
        return userIp;
    }
}