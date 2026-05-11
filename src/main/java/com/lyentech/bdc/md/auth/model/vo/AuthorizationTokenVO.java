package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author 260583
 */
@Data
public class AuthorizationTokenVO {

    private String access_token;

    private String token_type;

    private Integer expires_in;
}
