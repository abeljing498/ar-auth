package com.lyentech.bdc.md.auth.common.constant;

public enum RoleOperationType {

    UPDATE("UPDATE", "角色修改"),
    ADD("ADD", "角色增加"),
    DELETE("DELETE", "角色删除"),
    STATUS_CHANGE("STATUS_CHANGE", "角色状态变更"),
    USER_CHANGE("USER_CHANGE", "角色人员变更");



    private String code;
    private String message;

    private RoleOperationType(String code, String message) {
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
