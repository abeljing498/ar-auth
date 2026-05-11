package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/7/4
 * @create 14:56
 */
@Data
@TableName("md_app_sso")
public class MdAppSso implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "app_id")
    private String appId;

    @TableField(value = "sso_app_id")
    private String ssoAppId;

    @TableField(value = "sso_app_key")
    private String ssoAppKey;
}
