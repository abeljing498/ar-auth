package com.lyentech.bdc.md.auth.common.constant;

import com.lyentech.bdc.http.response.ResultCode;

public enum ResultCodeExtend  {
    REQUEST_SUCCESS(1000, "请求成功"),
    ILLEGAL_PARAM_FAILED(2002, "非法参数"),
    SERVER_ERROR(3000, "服务器错误"),
    TOKEN_INVALID(4001, "TOKEN 失效"),
    SESSION_INVALID(4002, "Session 失效"),
    UNAUTHORIZED(4003, "未授权"),
    REDIRECT(5000, "重定向"),
    FORWARD(5001, "转发"),
    BUSINESS_FAIL(6000, "业务异常"),
    SSO_LOGIN_GET_USERINFO_FAIL(5002, "单点登录用户信息获取失败"),
    NOT_ROLE_LOGIN_FAIL(4004, "无权登录系统"),
    UNKNOWN_FAILED(9000, "服务器异常，请稍后重试！");


    private Integer code;
    private String message;

    private ResultCodeExtend(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
