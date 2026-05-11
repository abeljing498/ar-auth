package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

@Data
public class ManagerResetPasswordParam {
    /**
     * 用户ID
     */
    Long userId;
    /**
     * 管理员ID
     */
    Long manageId;

    /**
     * 角色id，约定的id
     */
    Long roleId;
    /**
     * 密码
     */
    String password;
    /**
     * 项目ID
     */
    String appKey;
}
