package com.lyentech.bdc.md.auth.config.security.login.sms;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author guolanren
 */
public class MdSmsAuthenticationToken extends AbstractAuthenticationToken {

    private String phone;
    private String smsCode;
    private String clientId;
    private String loginSystem;
    private String loginBrowser;
    private String userIp;

    public MdSmsAuthenticationToken(String phone, String smsCode, String clientId, String loginSystem, String loginBrowser, String userIp) {
        super(null);
        this.phone = phone;
        this.smsCode = smsCode;
        this.clientId = clientId;
        this.loginBrowser=loginBrowser;
        this.loginSystem=loginSystem;
        this.userIp=userIp;
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
    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public String getClientId(){
        return clientId;
    }
}
