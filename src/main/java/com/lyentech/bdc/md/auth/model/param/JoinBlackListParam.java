package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 260583
 */
@Data
public class JoinBlackListParam implements Serializable {

    List<JoinBlackUserParam> joinUserList;

    String appId;

    Long oid;
}
