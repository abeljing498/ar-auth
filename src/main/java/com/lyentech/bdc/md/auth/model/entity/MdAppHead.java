package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/7/27
 * @create 8:38
 */
@TableName("md_app_head")
@Data
public class MdAppHead implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    /**
     * 获取负责人名称
     */
    @TableField("name")
    private String name;

    /**
     * 获取负责人手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 获取负责人邮箱
     */
    @TableField("email")
    private String email;

    @TableField("app_id")
    private String appId;
}
