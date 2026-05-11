package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author yan
 * @since 2022-05-24
 */
@TableName("md_user_org")
public class MdUserOrg implements Serializable {

    private static final long serialVersionUID = 6020091016997246437L;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;



    /**
     * org ID
     */
    @TableField("org_id")
    private Long orgId;

    @TableField("tenant_id")
    private Long tenantId;
    @TableField("status")
    private Integer status;
    @TableField(value = "create_time")
    private Date createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MdUserOrg{" +
                "userId=" + userId +
                ", orgId=" + orgId +
                ", tenantId=" + tenantId +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
