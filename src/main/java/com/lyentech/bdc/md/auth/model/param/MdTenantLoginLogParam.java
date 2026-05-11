package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

@Data
public class MdTenantLoginLogParam {
    private Long tenantId;
    private String tenantName;
    private String appKey;
    private Long userId;
    private String loginWay;
    private String operateSystem;
    private String browser;
}
