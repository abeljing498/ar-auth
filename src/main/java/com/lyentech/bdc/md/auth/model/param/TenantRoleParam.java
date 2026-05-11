package com.lyentech.bdc.md.auth.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @Author :yan
 * @Date :Create in 2022/9/19
 * @Description :
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRoleParam {

    /**
     * 角色Id
     */
    Long id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 租客Id
     */
    private Long tenantId;

    /**
     * 角色赋予的权限Ids
     */
    private List<Long> authList;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 角色状态
     */
    private Boolean status;

    /**
     * 页数
     */
    private Long pageNum;

    /**
     * 页码
     */
    private Long pageSize;
}
