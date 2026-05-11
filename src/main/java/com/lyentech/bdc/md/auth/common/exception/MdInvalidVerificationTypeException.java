package com.lyentech.bdc.md.auth.common.exception;

import com.lyentech.bdc.exception.BusinessException;

/**
 * @author guolanren
 */
public class MdInvalidVerificationTypeException extends BusinessException {

    public MdInvalidVerificationTypeException() {
        super();
    }

    public MdInvalidVerificationTypeException(String message, Throwable cause,
                                              boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MdInvalidVerificationTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdInvalidVerificationTypeException(String message) {
        super(message);
    }

    public MdInvalidVerificationTypeException(Throwable cause) {
        super(cause);
    }

}
