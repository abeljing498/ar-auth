package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author :yan
 * @Date :Create in 2022/5/24
 * @Description :
 */

@Data
public class OrgParam implements Serializable {

    private static final long serialVersionUID = 2655490771583517916L;

    private Long id;

    /**
     * 父id
     */
    private Long pid;

    /**
     * 负责人
     */
    private String director;

    /**
     * 负责人电话
     */
    private String phone;

    /**
     * 描述
     */
    private String description;

    /**
     * 自定义Id
     */
    private String customId;

    /**
     * 组织名
     */
    private String name;

    private Long tenantId;

    private Long pageNum;

    private Long pageSize;

    private Integer type;
    /**
     * 组织名拼音
     */
    private String orgPinyin;

    /**
     * 组织名多音字拼音
     */
    private String orgPinyin2;
    private Long operateUserId;
    private String operateUserName;
    private String userIp;
    private String appId;

}
