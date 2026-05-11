package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdAfsCheckException extends MdAuthenticationException {

    public MdAfsCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdAfsCheckException(String message) {
        super(message);
    }

}
