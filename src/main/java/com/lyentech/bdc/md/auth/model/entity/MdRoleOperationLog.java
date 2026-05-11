package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("md_role_operation_log")
public class MdRoleOperationLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    /**
     * 用户ID
     * @return
     */
    @TableField("app_id")
    String appId;
    @TableField("operation_user_id")
    Long operationUserId;
    @TableField("operation_user_name")
    String operationUserName;
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
    @TableField("role_id")
    Long roleId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    Date createTime;

}
