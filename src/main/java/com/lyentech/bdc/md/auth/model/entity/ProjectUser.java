package com.lyentech.bdc.md.auth.model.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProjectUser implements Serializable {
    Long userId;
    Long auUserId;
    Long tenantId;
    String email;
    Long orgId;
    Long roleId;
}
