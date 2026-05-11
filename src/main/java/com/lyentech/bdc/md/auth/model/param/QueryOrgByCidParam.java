package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

@Data
public class QueryOrgByCidParam {
    private String customId;
    private Long tenantId;
}
