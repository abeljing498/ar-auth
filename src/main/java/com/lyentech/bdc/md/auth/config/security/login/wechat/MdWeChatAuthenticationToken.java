package com.lyentech.bdc.md.auth.config.security.login.wechat;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author guolanren
 */
public class MdWeChatAuthenticationToken extends AbstractAuthenticationToken {

    private String clientId;
    private String wechatCode;
    private String loginSystem;
    private String loginBrowser;
    private String userIp;

    public MdWeChatAuthenticationToken(  String clientId, String wechatCode,String loginSystem,String loginBrowser,String userIp) {
        super(null);
        this.clientId = clientId;
        this.wechatCode = wechatCode;
        this.loginSystem=loginSystem;
        this.loginBrowser=loginBrowser;
        this.userIp=userIp;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return wechatCode;
    }

    public String getWechatCode() {
        return wechatCode;
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
