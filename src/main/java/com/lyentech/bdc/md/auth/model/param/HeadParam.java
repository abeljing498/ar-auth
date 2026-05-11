package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YuYi
 * @create 2022/7/1
 * @create 13:40
 */
@Data
public class HeadParam implements Serializable {
    private static final long serialVersionUID = 2001455843107079128L;
    private String name;
    private String headName;

    private String headPhone;

    private String headEmail;
}
