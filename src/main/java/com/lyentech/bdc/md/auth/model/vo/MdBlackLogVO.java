package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * @author YuYi
 * @create 2023/4/14
 * @create 10:51
 */
@Data
public class MdBlackLogVO {
    private Long id;

    private Long oid;

    private String operName;

    private String reason;

    private String type;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
