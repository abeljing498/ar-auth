package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author guolanren
 */
@TableName("md_menu")
public class MdMenu implements Serializable {

    private static final long serialVersionUID = 5818125490791127400L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("path")
    private String path;

    @TableField("parent_id")
    private Long parentId;

    private Set<MdMenu> children;

    private Meta meta;

    @TableField("button_sign")
    private String sign;

    @TableField("backlink")
    private String backlink;

    @TableField("icon")
    private String icon;

    @TableField("type")
    private String type;

    @TableField("menu_order")
    private Integer menuOrder;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Set<MdMenu> getChildren() {
        return children;
    }

    public void setChildren(Set<MdMenu> children) {
        this.children = children;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public String getBacklink() {
        return backlink;
    }

    public void setBacklink(String backlink) {
        this.backlink = backlink;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MdMenu mdMenu = (MdMenu) o;
        return Objects.equals(id, mdMenu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MdMenu{");
        sb.append("id=").append(id);
        sb.append(", path='").append(path).append('\'');
        sb.append(", parentId=").append(parentId);
        sb.append(", children=").append(children);
        sb.append(", meta=").append(meta);
        sb.append(", sign=").append(sign);
        sb.append(", backlink=").append(backlink);
        sb.append(", icon=").append(icon);
        sb.append(",type").append(type);
        sb.append(",menuOrder").append(menuOrder);
        sb.append('}');
        return sb.toString();
    }

    public static class Meta implements Serializable {

        private static final long serialVersionUID = -1651276882968294460L;

        private String title;

        private Boolean isMenu;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Boolean getIsMenu() {
            return isMenu;
        }

        public void setIsMenu(Boolean isMenu) {
            this.isMenu = isMenu;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Meta{");
            sb.append("title='").append(title).append('\'');
            sb.append(", isMenu=").append(isMenu);
            sb.append('}');
            return sb.toString();
        }
    }
}
