package com.lyentech.bdc.md.auth.model.entity;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@TableName("md_tenant")
@Data
public class Tenant implements Serializable{

    private static final long serialVersionUID = 5270189666589297547L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户名
     */
    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;


    @TableField(value = "status")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
