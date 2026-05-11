package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author iceWang
 * @date 2020/9/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MdRoleVO implements Serializable {
    private  Long id;
    private String name;
    private String appId;
    private String description;
    Boolean status;
    private String creator;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
