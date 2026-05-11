package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author yuyi
 */
@Data
public class QueryOrgNameParam {
    List<Long> ids;
    Long height;
}
