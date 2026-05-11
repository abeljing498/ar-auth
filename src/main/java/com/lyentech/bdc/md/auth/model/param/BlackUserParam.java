package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/4/13
 * @create 11:10
 */
@Data
public class BlackUserParam {

    private Long id;

    private String reason;

    private String appId;

    private Long userId;

    private Long oid;

    private Long blackId;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    private Long tenantId;
}
