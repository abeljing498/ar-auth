package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author 260583
 */
@Data
public class ListUserInfoParam {

    List<String> keys;

    String type;

    Long tenantId;

    Long height;
}
