package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

@Data
public class OrgMsgResult {
    private long childOrgId;
    private String msg;
    private  Boolean isMatch;

    // 构造函数、getter 和 setter 方法省略
}