package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("md_user_operation_log")
public class MdUserOperationLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    /**
     * 用户ID
     * @return
     */
    @TableField("user_id")
    Long userId;
    @TableField("app_id")
    String appId;
    @TableField("operate_user_id")
    Long operateUserId;
    @TableField("operate_user_name")
    String operateUserName;
    @TableField("tenant_id")
    Long tenantId;
    @TableField("notes")
    String notes;
    @TableField("user_ip")
    String userIp;
    @TableField("is_success")
    String isSuccess;
    @TableField("operation_type")
    String operationType;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    Date createTime;

}
