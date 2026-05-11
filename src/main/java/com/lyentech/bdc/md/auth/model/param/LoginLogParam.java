package com.lyentech.bdc.md.auth.model.param;

import lombok.Builder;
import lombok.Data;

@Data
public class LoginLogParam {

    Long pageSize;

    Long pageNum;

    String appKey;
    /**
     * 开始时间
     */
    String beginTime;
    /**
     * 结束时间
     */
    String endTime;
    /**
     * 用户ID或用户名
     */
    String user;
    /**
     * 登录方式
     */
    String loginWay;
    /**
     * 用户ID
     */
    Long id;
    private Long tenantId;
    private String tenantName;
}
