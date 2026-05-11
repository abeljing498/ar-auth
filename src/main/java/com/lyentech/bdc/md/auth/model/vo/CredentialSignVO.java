package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author 260583
 */
@Data
public class CredentialSignVO {
    String credential;

    Long timeStamp;

    String msg;

}
