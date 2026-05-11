package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@TableName("md_role_auth")
public class RoleAuth implements Serializable{

    private static final long serialVersionUID = -5706547053834829495L;

    /**
     * 角色id
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * auth ID
     */
    @TableField("auth_id")
    private Long authId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }


    @Override
    public String toString() {
        return "RoleAuth{" +
            "roleId=" + roleId +
            ", authId=" + authId +
        "}";
    }
}
