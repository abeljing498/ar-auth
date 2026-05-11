package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdLoginSsoErrorException extends MdAuthenticationException {

    public MdLoginSsoErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginSsoErrorException(String message) {
        super(message);
    }

}
