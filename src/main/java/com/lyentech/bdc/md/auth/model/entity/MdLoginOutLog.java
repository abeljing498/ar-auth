package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("md_login_out_log")
public class MdLoginOutLog {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    @TableField("user_id")
    Long userId;
    @TableField("user_name")
    String userName;
    @TableField("tenant_id")
    Long tenantId;

    /**
     * 登录IP
     */
    @TableField("ip")
    String ip;
    @TableField("is_success")
    Integer isSuccess;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    Date createTime;

    @TableField(value = "app_id")
    String appId;

}
