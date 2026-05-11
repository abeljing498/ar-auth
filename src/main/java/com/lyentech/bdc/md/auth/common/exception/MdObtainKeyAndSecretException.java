package com.lyentech.bdc.md.auth.common.exception;

import com.lyentech.bdc.exception.BusinessException;

/**
 * @author guolanren
 */
public class MdObtainKeyAndSecretException extends BusinessException {

    public MdObtainKeyAndSecretException() {
        super();
    }

    public MdObtainKeyAndSecretException(String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MdObtainKeyAndSecretException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdObtainKeyAndSecretException(String message) {
        super(message);
    }

    public MdObtainKeyAndSecretException(Throwable cause) {
        super(cause);
    }

}
