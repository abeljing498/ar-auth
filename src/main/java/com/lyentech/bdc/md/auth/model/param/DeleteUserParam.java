package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

@Data
public class DeleteUserParam {
    Long userId;
    Long orgId;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    private String appId;
    private Long tenantId;
}
