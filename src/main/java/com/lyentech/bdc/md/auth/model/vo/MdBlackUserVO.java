package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * @author YuYi
 * @create 2023/4/17
 * @create 10:21
 */
@Data
public class MdBlackUserVO {

    private Long id;

    private Long userId;

    private String nickname;


    private String phone;
    private Integer blackNum;

    private String email;

    private String operName;

    private String reason;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String account;

}
