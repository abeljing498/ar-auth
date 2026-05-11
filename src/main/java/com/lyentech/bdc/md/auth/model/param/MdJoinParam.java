package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;
/**
 * @author guolanren
 */
public class MdJoinParam implements Serializable {

    private static final long serialVersionUID = -4987166591569088206L;

    private String phone;

    private String password;
    private String nickname;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    private String account;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    private String appId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    private String employeeId;
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public String getSmsCode() {
//        return smsCode;
//    }
//
//    public void setSmsCode(String smsCode) {
//        this.smsCode = smsCode;
//    }
//
//    public String getReturnTo() {
//        return returnTo;
//    }
//
//    public void setReturnTo(String returnTo) {
//        this.returnTo = returnTo;
//    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
