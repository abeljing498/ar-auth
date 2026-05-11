package com.lyentech.bdc.md.auth.common.context;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 授权后得到的用户内容实体
 *
 * @author guolanren
 */
public class MdUserInfo implements Serializable {

    private static final long serialVersionUID = -7390238559943091878L;
    private static MdUserInfo anonymousUserInfo;
    private Long id;
    private String phone;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private Set<String> roles;
    private Map<String, Object> additional;
    private String moreInfo;

    public static MdUserInfo anonymousUserInfo() {
        if (anonymousUserInfo == null) {
            MdUserInfo anonymous = new MdUserInfo();
            anonymous.setId(0L);
            anonymous.setUsername("MD_0");

            Set<String> roles = new HashSet<>();
            roles.add("anonymous");
            anonymous.setRoles(roles);
            anonymousUserInfo = anonymous;
        }
        return anonymousUserInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
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

    public Map<String, Object> getAdditional() {
        return additional;
    }

    public void setAdditional(Map<String, Object> additional) {
        this.additional = additional;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }
}
