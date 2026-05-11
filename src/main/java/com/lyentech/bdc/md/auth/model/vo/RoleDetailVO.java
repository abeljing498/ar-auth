package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色
 * @author YaoYulong
 */
@Data
public class RoleDetailVO implements Serializable {
    /**
     * 角色Id
     */
    Long id;
    /**
     * 角色名
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色赋予的权限Ids
     */
    private List<Long> authList;

    private List<AuthVO> authTreeList;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 角色状态
     */
    private Boolean status;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}