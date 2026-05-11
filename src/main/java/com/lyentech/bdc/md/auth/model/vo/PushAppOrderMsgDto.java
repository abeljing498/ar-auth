package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author 260583
 */
@Data
public class PushAppOrderMsgDto {

    private String appKey;
    private String tenantId;
    private String userId;
    private String message;
    private String token;
    private String roleId;
}
