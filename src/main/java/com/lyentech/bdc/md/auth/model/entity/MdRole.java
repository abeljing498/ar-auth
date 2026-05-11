package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author guolanren
 */
@TableName("md_role")
public class MdRole implements Serializable {

    private static final long serialVersionUID = 4941275271231691145L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 角色名
     */
    @TableField("name")
    private String name;
    @TableField("app_id")
    private String appId;
    @TableField(select = false)
    private Set<String> permissions;
    @TableField(select = false)
    private Set<MdMenu> menus;
    /**
     * 角色描述
     */
    @TableField("description")
    private String description;
    /**
     * 角色类型 ：权限个数 1|2|3|4...
     */
    @TableField("type")
    private int type;
    /**
     * 角色状态是否关闭默认打开，true
     */
    @TableField(value = "status" , fill = FieldFill.DEFAULT)
    private Boolean status;

    @TableField(value = "version" , fill = FieldFill.DEFAULT)
    private Integer version;


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 创建者名称
     */

    @TableField("create_by")
    private Long createBy;
    /**
     * 创建时间
     */
    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<MdMenu> getMenus() {
        return menus;
    }

    public void setMenus(Set<MdMenu> menus) {
        this.menus = menus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MdRole mdRole = (MdRole) o;
        return Objects.equals(id, mdRole.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MdRole{");
        sb.append("name='").append(name).append('\'');
        sb.append(", permissions=").append(permissions);
        sb.append(", menus=").append(menus);
        sb.append('}');
        return sb.toString();
    }
}
