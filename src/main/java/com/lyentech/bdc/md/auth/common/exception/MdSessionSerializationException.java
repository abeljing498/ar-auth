package com.lyentech.bdc.md.auth.common.exception;

/**
 * @author guolanren
 */
public class MdSessionSerializationException extends RuntimeException {

    public MdSessionSerializationException() {
        super();
    }

    public MdSessionSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdSessionSerializationException(String message) {
        super(message);
    }

    public MdSessionSerializationException(Throwable cause) {
        super(cause);
    }

}
