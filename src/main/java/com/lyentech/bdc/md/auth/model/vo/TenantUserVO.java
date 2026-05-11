package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

@Data
public class TenantUserVO {
    private Long id;
    private Long orgId;
    private String name;
    private String phone;
    private String email;
}
