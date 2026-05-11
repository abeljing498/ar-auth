package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdLoginSmsErrorException extends MdAuthenticationException {

    public MdLoginSmsErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginSmsErrorException(String message) {
        super(message);
    }

}
