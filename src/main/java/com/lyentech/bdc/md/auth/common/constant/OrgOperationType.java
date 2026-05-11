package com.lyentech.bdc.md.auth.common.constant;

public enum OrgOperationType {

    UPDATE("UPDATE", "组织修改"),
    ADD("ADD", "组织增加"),
    DELETE("DELETE", "组织删除");



    private String code;
    private String message;

    private OrgOperationType(String code, String message) {
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
