package com.lyentech.bdc.md.auth.config.security.login.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author guolanren
 */
public class MdAuthenticationException extends AuthenticationException {

    public MdAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdAuthenticationException(String message) {
        super(message);
    }

}
