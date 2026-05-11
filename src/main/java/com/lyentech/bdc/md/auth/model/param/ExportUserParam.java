package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.util.List;

@Data
public class ExportUserParam {
    private String appId;
    private Long tenantId;
    private Long userId;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    List<Long> userIds;
}
