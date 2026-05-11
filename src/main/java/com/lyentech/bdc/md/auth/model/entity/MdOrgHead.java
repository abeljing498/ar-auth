package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/8/8
 * @create 16:34
 */
@Data
@TableName("md_org_head")
public class MdOrgHead implements Serializable {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @TableField("director")
    private String director;

    @TableField("phone")
    private String phone;

    @TableField("org_id")
    private Long orgId;
}
