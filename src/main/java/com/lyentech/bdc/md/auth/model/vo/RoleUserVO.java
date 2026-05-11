package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

@Data
public class RoleUserVO {
    String appId;
    Long tenantId;
    Long orgId;
    Long roleId;
    Long userId;
}
