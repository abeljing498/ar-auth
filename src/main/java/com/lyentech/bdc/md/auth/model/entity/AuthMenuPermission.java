package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("md_auth_menu_permission")
public class AuthMenuPermission  implements Serializable{

    private static final long serialVersionUID = 6759104235597995505L;

    /**
     * 菜单id
     */
    @TableField("menu_id")
    private Long menuId;

    /**
     * permission ID
     */
    @TableField("permission_id")
    private Long permissionId;

    /**
     * auth ID
     */
    @TableField("auth_id")
    private Long authId;

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }

    @Override
    public String toString() {
        return "AuthMenuPermission{" +
            "menuId=" + menuId +
            ", permission=" + permissionId +
            ", authId=" + authId +
        "}";
    }
}
