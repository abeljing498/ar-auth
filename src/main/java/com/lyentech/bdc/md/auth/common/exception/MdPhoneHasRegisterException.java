package com.lyentech.bdc.md.auth.common.exception;

import com.lyentech.bdc.exception.BusinessException;

/**
 * @author guolanren
 */
public class MdPhoneHasRegisterException extends BusinessException {

    public MdPhoneHasRegisterException() {
        super();
    }

    public MdPhoneHasRegisterException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MdPhoneHasRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdPhoneHasRegisterException(String message) {
        super(message);
    }

    public MdPhoneHasRegisterException(Throwable cause) {
        super(cause);
    }

}
