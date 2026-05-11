package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 260583
 */
@Data
public class UserInfoVO {

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
     * 用户账号
     */
    private String account;

    /**
     * 员工所有角色
     */
    private List<Long> roleList;

    /**
     * 员工所属所有组织
     */
    private List<OrgNewVO> orgList;

    /**
     * 提示（若用户已存在于Au账号池的不同租户中）
     */
    private String logo;

}
