package com.lyentech.bdc.md.auth.tencent.exception;

import com.lyentech.bdc.exception.ServerErrorException;

/**
 * @author guolanren
 */
public class MdAfsException extends ServerErrorException {

    public MdAfsException(String message) {
        super(message);
    }

    public MdAfsException(String message, Throwable e) {
        super(message, e);
    }
}
