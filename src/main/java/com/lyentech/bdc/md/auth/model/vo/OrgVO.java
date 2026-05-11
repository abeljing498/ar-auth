package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author :yan
 * @Date :Create in 2022/6/17
 * @Description :
 */

@Data
public class OrgVO implements Serializable {

    private Long id;

    private Long pid;

    private Long height;

    private String pidName;

    /**
     * 自定义Id
     */
    private String customId;

    private String tenantId;

    /**
     * 负责人
     */
    private String director;

    /**
     * 负责人电话
     */
    private String phone;

    /**
     * 描述
     */
    private String description;

    /**
     * 组织名
     */
    private String name;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private Integer userNum;
}
