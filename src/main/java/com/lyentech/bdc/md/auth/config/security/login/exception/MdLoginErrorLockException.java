package com.lyentech.bdc.md.auth.config.security.login.exception;

/**
 * @author YuYi
 * @create 2023/3/7
 * @create 14:55
 */
public class MdLoginErrorLockException extends MdAuthenticationException {
    public MdLoginErrorLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdLoginErrorLockException(String message) {
        super(message);
    }
}
