package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/12/8
 * @create 9:21
 */
@Data
public class AuthOldVO implements Serializable {
    Long id;
    String name;
    Boolean status;
}
