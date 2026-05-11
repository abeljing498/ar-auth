package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.util.List;

@Data
public class UserTenantRoleParam {
    /**
     * AppId
     */
    private String appId;
    /**
     * 员工Id
     */
    private Long userId;

    private List<Long> roleList;
    /**
     * 租户Id
     */
    private Long tenantId;
}
