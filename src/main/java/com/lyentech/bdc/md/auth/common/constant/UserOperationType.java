package com.lyentech.bdc.md.auth.common.constant;

public enum UserOperationType {
    BLACK("BLACK", "用户加入黑名单"),
    UNLACK("UNLACK", "用户解除黑名单"),
    UPDATE("UPDATE", "用户信息修改"),
    MANGER_PW("MANGER_PW", "管理员修改密码"),
    CODE_PW("CODE_PW", "短信修改密码"),
    ADD("ADD", "用户管理员添加"),
    SELF_ADD("SELF_ADD", "用户自注册"),
    DELETE("DELETE", "用户删除"),
    EXPORT("EXPORT", "用户导出");


    private String code;
    private String message;

    private UserOperationType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
