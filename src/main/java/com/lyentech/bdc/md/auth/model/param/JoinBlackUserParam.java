package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author 260583
 */
@Data
public class JoinBlackUserParam  {
    private String reason;

    private Long userId;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    private Long tenantId;
    private String appId;
}
