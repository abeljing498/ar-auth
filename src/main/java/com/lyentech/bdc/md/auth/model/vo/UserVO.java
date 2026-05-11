package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author yuyi
 */
@Data
public class UserVO {
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
     * 账户密码
     */
    private String account;
    /**
     * email
     */
    private String email;

}
