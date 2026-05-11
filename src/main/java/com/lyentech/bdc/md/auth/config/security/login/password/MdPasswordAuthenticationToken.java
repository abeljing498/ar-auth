package com.lyentech.bdc.md.auth.config.security.login.password;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author guolanren
 */
public class MdPasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public MdPasswordAuthenticationToken(String phone, String password) {
        super(phone, password);
    }

    @Override
    public Object getCredentials() {
        return super.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return super.getPrincipal();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        super.setAuthenticated(isAuthenticated);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }

}
