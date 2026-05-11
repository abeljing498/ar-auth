package com.lyentech.bdc.md.auth.config.security.login.unicom;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author guolanren
 */
public class MdUnicomAuthenticationToken extends AbstractAuthenticationToken {

    private String callback;
    private String clientId;

    public MdUnicomAuthenticationToken(String callback, String clientId) {
        super(null);
        this.callback = callback;
        this.clientId = clientId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return callback;
    }

    public String getClientId() {
        return clientId;
    }
}
