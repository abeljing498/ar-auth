package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserListVO {
    private Long orgId;

    private String orgName;

    private Long id;
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
     * 员工所属所有组织
     */
    private List<OrgNewVO> orgList;
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

    private List<Long> roleIdList;
    private List<String> roleNameList;
}
