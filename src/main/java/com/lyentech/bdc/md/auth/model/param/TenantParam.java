package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;


/**
 * @author YuYi
 * @create 2023/3/29
 * @create 9:25
 */
@Data
public class TenantParam {
    /**
     * 租户id
     */
    Long id;

    /**
     * 项目appId
     */
    String appId;

    /**
     * 租户名称
     */
    String name;

    /**
     * 租户描述
     */
    String description;

    /**
     * 是否维护
     */
    Boolean authApp;

    /**
     * 租户状态
     */
    Boolean status;

    Long userId;

    Boolean autoRole;

    public Boolean getAutoRole() {
        if (autoRole == null) {
            return true;
        }
        return autoRole;
    }

    public void setAutoRole(Boolean autoRole) {
        if (autoRole == null) {
            return;
        }
        this.autoRole = autoRole;
    }
}
