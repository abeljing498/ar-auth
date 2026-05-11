package com.lyentech.bdc.md.auth.config.security.login.exception;

public class MdLoginPasswordTimeOutException extends MdAuthenticationException{
    public MdLoginPasswordTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginPasswordTimeOutException(String message) {
        super(message);
    }
}
