package com.lyentech.bdc.md.auth.common.constant;

public enum LoginResultType {
    SUCCESS(1, "登录成功"),
    FAIL(0, "登录失败"),
    ;

    private LoginResultType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
