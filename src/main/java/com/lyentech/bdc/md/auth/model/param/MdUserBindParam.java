package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/3/22
 * @create 9:48
 */
@Data
public class MdUserBindParam {

    private Long userId;

    private String phone;

    private String email;
}
