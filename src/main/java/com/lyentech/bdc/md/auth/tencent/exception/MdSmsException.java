package com.lyentech.bdc.md.auth.tencent.exception;

import com.lyentech.bdc.exception.ServerErrorException;

/**
 * @author guolanren
 */
public class MdSmsException extends ServerErrorException {

    public MdSmsException(String message) {
        super(message);
    }

    public MdSmsException(String message, Throwable e) {
        super(message, e);
    }
}
