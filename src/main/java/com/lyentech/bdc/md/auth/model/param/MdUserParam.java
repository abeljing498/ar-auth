package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;

/**
 * @author guolanren
 */
public class MdUserParam implements Serializable {

    private static final long serialVersionUID = -6157597451767461621L;

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
