package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author YuYi
 * @create 2022/8/31
 * @create 11:44
 */
@Data
@TableName("md_app_login_method")
public class MdAppLoginMethod {

    @TableId(value = "app_id")
    private String appId;

    @TableField("is_sso")
    private Boolean sso;

    @TableField("is_account")
    private Boolean account;

    @TableField("is_code")
    private Boolean code;

    @TableField("is_sign")
    private Boolean sign;
}
