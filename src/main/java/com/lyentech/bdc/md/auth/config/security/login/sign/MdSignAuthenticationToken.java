package com.lyentech.bdc.md.auth.config.security.login.sign;

import com.tencentcloudapi.common.Sign;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author 260583
 */
public class MdSignAuthenticationToken extends AbstractAuthenticationToken {
    private String clientId;
    private String credential;

    public MdSignAuthenticationToken(String clientId, String credential) {
        super(null);
        this.clientId = clientId;
        this.credential = credential;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCredential() {
        return credential;
    }

}
