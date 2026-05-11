package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 组织下用户信息
 * @author YaoYulong
 */
@Data
public class OrgUserParam implements Serializable {

    /**
     * AppId
     */
    private String appId;
    /**
     * 员工Id
     */
    private Long id;
    /**
     * 员工电话号码
     */
    private String phone;

    private String account;
    /**
     * 员工姓名
     */
    private String nickname;
    /**
     * 用户账号
     */
    private String userName;
    /**
     * 员工邮箱号
     */
    private String email;

    private Long orgId;
    /**
     * 员工所属组织Id
     */
    private Set<Long> orgIds;
    /**
     * 角色Id
     */
    private Set<Long> roleList;
    /**
     * 租户Id
     */
    private Long tenantId;

    /**
     * 员工姓名拼音
     */
    private String pinyin;
    /**
     * 员工姓名多音字拼音
     */
    private String pinyin2;
    private Boolean isCheckAccount;

    private Long pid;

    List<Long> channelIds;

    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    private String employeeId;
    private Boolean permissionChange;
}
