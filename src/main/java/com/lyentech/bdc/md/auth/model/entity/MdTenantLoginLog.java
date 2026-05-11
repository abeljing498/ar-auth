package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("md_tenant_login_log")
public class MdTenantLoginLog {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    @TableField("user_id")
    Long userId;
    /**
     * 租户Id
     */
    @TableField("tenant_id")
    Long tenantId;
    /**
     * 租户名称
     */
    @TableField("tenant_name")
    String tenantName;
    /**
     * 登录IP
     */
    @TableField("ip")
    String ip;

    /**
     * 浏览器
     */
    @TableField("browser")
    String browser;

    /**
     * 操作系统
     */
    @TableField("operate_system")
    String operateSystem;

    /**
     * 登录方式
     */
    @TableField("login_way")
    String loginWay;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    Date createTime;

    @TableField(value = "app_id")
    String appId;

}
