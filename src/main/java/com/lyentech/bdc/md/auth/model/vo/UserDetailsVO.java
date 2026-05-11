package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
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
public class UserDetailsVO implements Serializable {

    private Long orgId;

    private String orgName;

    private Long id;

    private Long tenantId;

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
     * 用户账号
     */
    private String account;

    /**
     * 员工所属所有组织
     */
    private List<OrgNewVO> orgList;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date tenantTime;

    /**
     * 员工所属所有角色ids
     */
    @JSONField(serialize = false)
    private String roleIds;

    /**
     * 员工创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 员工在当前组织下的角色
     */
    @JSONField(serialize = false)
    private String name;

    private List<Long> roleList;

    private List<String> roleName;

    /**
     * 员工姓名拼音
     */
    private String pinyin;
    /**
     * 员工姓名多音字拼音
     */
    private String pinyin2;
    /**
     * 用户状态
     */
    private String status;
    /**
     * 用户群体
     */
    private List<Long> channelIds;
    private List<String> channelName;

}
