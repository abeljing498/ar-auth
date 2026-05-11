package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/4/4
 * @create 18:35
 */
@Data

public class ExcelImportTeamUserParam {
    private String nickname;
    private String phone;
    private String email;
    private String orgName;
    private Long orgId;
    private Long roleId;
    private String roleName;
    private Integer channelId;
    private String channelName;
    /**
     * 状态
     */
    private String status;
    /**
     * 失败原因
     */
    private String failReason;
}
