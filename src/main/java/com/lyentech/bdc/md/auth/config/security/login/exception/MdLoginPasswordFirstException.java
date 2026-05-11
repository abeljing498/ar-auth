package com.lyentech.bdc.md.auth.config.security.login.exception;

public class MdLoginPasswordFirstException extends MdAuthenticationException{
    public MdLoginPasswordFirstException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginPasswordFirstException(String message) {
        super(message);
    }
}
