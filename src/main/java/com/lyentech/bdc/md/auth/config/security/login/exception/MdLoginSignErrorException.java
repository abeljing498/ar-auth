package com.lyentech.bdc.md.auth.config.security.login.exception;


/**
 * @author 260583
 */
public class MdLoginSignErrorException extends MdAuthenticationException {

    public MdLoginSignErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginSignErrorException(String message) {
        super(message);
    }
}
