package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author YuYi
 * @create 2022/11/25
 * @create 9:51
 */
@Data
public class GroupAuthsParam {
    private String groupName;
    private Long groupId;
    List<GroupAuthParam> authLists;

}
