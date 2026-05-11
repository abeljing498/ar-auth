package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户详细信息包含所有角色，所有组织
 *
 * @author Yaoyulong
 */
@Data
public class UserAppRolesVO implements Serializable {

    private String appId;
    private Long tenantId;
    private String appName;

    private Long userId;

    /**
     * 员工电话号码
     */
    private String phone;

    /**
     * 员工姓名
     */
    private String nickname;

    /**
     * 员工邮箱号
     */
    private String email;

    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 角色列表
     */
    private List<String> roleName;;


}
