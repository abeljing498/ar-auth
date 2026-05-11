package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/4/18
 * @create 11:06
 */
@Data
public class BlackLogParam {
    Long pageNum;
    Long pageSize;
    String appId;
    String type;
    Long userId;
    String beginTime;
    String endTime;
    String reason;
}
