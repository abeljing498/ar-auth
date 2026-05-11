package com.lyentech.bdc.md.auth.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author :yan
 * @Date :Create in 2022/9/22
 * @Description :
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryOrgUserParam {

    /**
     * 员工Id
     */
    private Long id;

    private String keyword;

    /**
     * 员工电话号码
     */
    private String account;
//
//    /**
//     * 员工姓名
//     */
//    private String nickname;
//
//    /**
//     * 员工邮箱号
//     */
//    private String email;

    /**
     * 租户Id
     */
    private Long tenantId;

    private Long orgId;

    private Long pageNum;

    private Long pageSize;
    private Integer userState;
}
