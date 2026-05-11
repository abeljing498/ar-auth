package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author guolanren
 */
public class MdLoginParamIllegalException extends MdAuthenticationException {

    public MdLoginParamIllegalException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginParamIllegalException(String message) {
        super(message);
    }

}
