package com.lyentech.bdc.md.auth.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("md_user_operate_log")
public class MdUserOperateLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    /**
     * 用户ID
     * @return
     */
    @TableField("user_id")
    Long userId;

    /**
     * 操作用户
     */
    @TableField("operate_user")
    Long operateUser;
    
    /**
     * 操作内容（密码、权限、账号）
     */
    @TableField("operate_content")
    String operateContent;

    /**
     * 修改内容（JSON格式实体类）
     */
    @TableField("operate_info")
    String operateInfo;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    Date createTime;

    /**
     * 操作类型：update/delete/add
     */
    @TableField("operate_type")
    String operateType;


}
