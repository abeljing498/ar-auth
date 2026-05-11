package com.lyentech.bdc.md.auth.config.security.login.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author guolanren
 */
public class MdLoginUnknowDestinationException extends AuthenticationException {

    public MdLoginUnknowDestinationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginUnknowDestinationException(String message) {
        super(message);
    }

}
