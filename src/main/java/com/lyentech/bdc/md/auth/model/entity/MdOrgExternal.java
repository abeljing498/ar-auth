package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yuyi
 */
@TableName("md_org_external")
@Data
public class MdOrgExternal implements Serializable {

    /**
     * 组织Id
     */
    @TableId(value = "org_id")
    private Long orgId;
    /**
     * 自定义Id
     */
    @TableField("custom_id")
    private String customId;

    @TableField("tenant_id")
    private Long tenantId;

    private static final long serialVersionUID = 5818125490791127400L;
}
