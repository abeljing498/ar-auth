package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdLoginPasswordErrorException extends MdAuthenticationException {

    public MdLoginPasswordErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginPasswordErrorException(String message) {
        super(message);
    }

}
