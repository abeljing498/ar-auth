package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdUnsupportLoginAuthTypeException extends MdAuthenticationException {

    public MdUnsupportLoginAuthTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdUnsupportLoginAuthTypeException(String message) {
        super(message);
    }

}
