package com.lyentech.bdc.md.auth.model.vo;

import com.lyentech.bdc.md.auth.model.entity.MdRole;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @Author :yan
 * @Date :Create in 2022/6/8
 * @Description :
 */

@Data
public class TenantRoleVO implements Serializable {

    private static final long serialVersionUID = 4150229472345783400L;

    private Long tenantId;

    /**
     * 租户名
     */
    private String name;

    private Map<String, Map<String, ?>> aclInfo;
}
