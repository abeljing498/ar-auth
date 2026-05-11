package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/6/24
 * @create 17:14
 */
@Data
public class AppHeadVO implements Serializable {
    private static final long serialVersionUID = 2001455843107079128L;

    private String name;

    private String phone;

    private String email;
}
