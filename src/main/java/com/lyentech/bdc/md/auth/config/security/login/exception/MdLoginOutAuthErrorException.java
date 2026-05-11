package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdLoginOutAuthErrorException extends MdAuthenticationException {

    public MdLoginOutAuthErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginOutAuthErrorException(String message) {
        super(message);
    }

}
