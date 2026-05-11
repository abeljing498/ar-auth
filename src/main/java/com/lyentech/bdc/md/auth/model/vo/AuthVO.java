package com.lyentech.bdc.md.auth.model.vo;

import com.lyentech.bdc.md.auth.model.param.GroupAuthParam;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthVO implements Serializable {
    String groupName;
    Long groupId;

    Integer num;

    Boolean isAll;
    List<GroupAuthParam> groupLists;
}
