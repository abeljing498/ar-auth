package com.lyentech.bdc.md.auth.common.exception;

import com.lyentech.bdc.exception.BusinessException;

/**
 * @author guolanren
 */
public class MdVerificationCodeException extends BusinessException {

    public MdVerificationCodeException() {
        super();
    }

    public MdVerificationCodeException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MdVerificationCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdVerificationCodeException(String message) {
        super(message);
    }

    public MdVerificationCodeException(Throwable cause) {
        super(cause);
    }

}
