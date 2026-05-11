package com.lyentech.bdc.md.auth.config.security.login.customer;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class MdCustomerAuthenticationToken extends AbstractAuthenticationToken {
    private String code;
    private String clientId;
    private String loginSystem;
    private String loginBrowser;
    private String userIp;
    private Long tenantId;
    public MdCustomerAuthenticationToken( String code, String clientId, String loginSystem, String loginBrowser, String userIp,Long tenantId) {
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