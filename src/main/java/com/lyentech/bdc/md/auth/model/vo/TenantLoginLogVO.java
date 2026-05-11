package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class TenantLoginLogVO {

    Long userId;

    String userName;
    /**
     * 租户Id
     */

    Long tenantId;
    /**
     * 租户名称
     */
    String tenantName;
    /**
     * 登录地
     */
    String region;

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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    Date createTime;
}
