package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author 260583
 */
@Data
public class PushMessageDto {
    private String tenantId;
    private String roleId;
    private String status;
    private String appKey;
    private String isInterface;
}
