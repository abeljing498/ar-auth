package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

@Data
public class RoleUserParam {
    String appId;
    Long tenantId;
    Long orgId;
    Long roleId;
    Long userId;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
}
