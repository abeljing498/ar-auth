package com.lyentech.bdc.md.auth.config.security.login.exception;

import javax.naming.AuthenticationException;

/**
 * @author YuYi
 * @create 2023/4/19
 * @create 14:57
 */
public class MdLoginBlackUserException extends MdAuthenticationException {
    public MdLoginBlackUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginBlackUserException(String message) {
        super(message);
    }
}
