package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2022/11/25
 * @create 9:49
 */
@Data
public class GroupAuthParam {
    private Long id;
    private String name;
    private Boolean authStatus;
}
