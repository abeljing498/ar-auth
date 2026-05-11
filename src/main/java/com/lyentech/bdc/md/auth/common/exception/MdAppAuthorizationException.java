package com.lyentech.bdc.md.auth.common.exception;

import com.lyentech.bdc.exception.BusinessException;

/**
 * @author guolanren
 */
public class MdAppAuthorizationException extends BusinessException {

    public MdAppAuthorizationException() {
        super();
    }

    public MdAppAuthorizationException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MdAppAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdAppAuthorizationException(String message) {
        super(message);
    }

    public MdAppAuthorizationException(Throwable cause) {
        super(cause);
    }

}
