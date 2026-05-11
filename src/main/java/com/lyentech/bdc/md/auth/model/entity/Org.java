package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@TableName("md_org")
public class Org implements Serializable{

    private static final long serialVersionUID = -885152477625574433L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父id
     */
    @TableField("pid")
    private Long pid;



    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 组织名
     */
    @TableField("name")
    private String name;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("height")
    private Long height;

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

    @TableField("org_pinyin")
    private String orgPinyin;

    public String getOrgPinyin() {
        return orgPinyin;
    }

    public void setOrgPinyin(String orgPinyin) {
        this.orgPinyin = orgPinyin;
    }

    public String getOrgPinyin2() {
        return orgPinyin2;
    }

    public void setOrgPinyin2(String orgPinyin2) {
        this.orgPinyin2 = orgPinyin2;
    }

    @TableField("org_pinyins")
    private String orgPinyin2;

    @TableField("org_order")
    private Long orgOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getOrgOrder() {
        return orgOrder;
    }

    public void setOrgOrder(Long orgOrder) {
        this.orgOrder = orgOrder;
    }

    @Override
    public String toString() {
        return "Org{" +
                "id=" + id +
                ", pid=" + pid +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", tenantId=" + tenantId +
                ", height=" + height +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
