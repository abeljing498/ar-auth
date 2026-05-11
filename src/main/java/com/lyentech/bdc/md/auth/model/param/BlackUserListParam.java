package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/4/17
 * @create 11:29
 */
@Data
public class BlackUserListParam {
    Long pageNum;
    Long pageSize;
    String appId;
    String keyword;
    String beginTime;
    String endTime;
    String reason;
}
