package com.lyentech.bdc.md.auth.model.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author guolanren
 */
@TableName(value = "md_user")
public class MdUser implements UserDetails, Serializable {

    private static final long serialVersionUID = 1014193695942898994L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("phone")
    private String phone;

    @TableField("password")
    @JSONField(serialize = false, deserialize = true)
    private String password;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "email")
    private String email;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;

    @TableField(select = false)
    private Set<String> roles;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(select = false)
    private Boolean sysAdmin;

    @TableField(value = "account")
    private String account;

    @TableField(exist = false)
    private Map<String, Object> additional;

    @TableField(select = false)
    private String ssoToken;

    @TableField(value = "pinyin")
    private String pinyin;

    @TableField(value = "pinyins")
    private String pinyin2;
    @TableField(value = "employee_id")
    private String employeeId;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinyin2() {
        return pinyin2;
    }

    public void setPinyin2(String pinyin2) {
        this.pinyin2 = pinyin2;
    }

    /**
     * 是否是密码第一次登录
     */
    @TableField("first_login")
    private Boolean firstLoginPW;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, Object> getAdditional() {
        return additional;
    }

    public void setAdditional(Map<String, Object> additional) {
        this.additional = additional;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.NO_AUTHORITIES;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return "MD_" + id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (deleted == null) {
            return true;
        }
        return !deleted;
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

    public Boolean getSysAdmin() {
        return sysAdmin;
    }

    public void setSysAdmin(Boolean sysAdmin) {
        this.sysAdmin = sysAdmin;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public Boolean getFirstLoginPW() {
        return firstLoginPW;
    }

    public void setFirstLoginPW(Boolean firstLoginPW) {
        this.firstLoginPW = firstLoginPW;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MdUser{");
        sb.append("id=").append(id);
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", deleted=").append(deleted);
        sb.append(", roles=").append(roles);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", additional=").append(additional);
        sb.append('}');
        return sb.toString();
    }
}
