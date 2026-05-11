package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 260583
 */
@Data
public class OrgHeightNameVO {
    List<String> orgNameLists;

    List<String> orgCustomIdLists;

    List<Long> orgIdLists;
}
