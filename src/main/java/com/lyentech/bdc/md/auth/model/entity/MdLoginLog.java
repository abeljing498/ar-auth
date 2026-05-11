package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("md_login_log")
public class MdLoginLog {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    @TableField("user_id")
    Long userId;

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
    @TableField(value = "is_success")
    Integer isSuccess;
    @TableField(value = "fail_reason")
    String failReason;
    @TableField(value = "account")
    String account;

}
