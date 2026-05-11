package com.lyentech.bdc.md.auth.model.vo;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author guolanren
 */
public class MdUserVO implements Serializable {

    private static final long serialVersionUID = 2001455843107079128L;

    private Long id;

    private String phone;

    @Override
    public String toString() {
        return "MdUserVO{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    private String nickname;

    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MdUserVO mdUserVO = (MdUserVO) o;
        return Objects.equals(id, mdUserVO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
