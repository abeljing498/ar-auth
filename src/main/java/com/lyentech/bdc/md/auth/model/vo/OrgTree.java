package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.fastjson.annotation.JSONField;

import com.lyentech.bdc.md.auth.util.BasicTree;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Data
public class OrgTree extends BasicTree implements Serializable{

    private static final long serialVersionUID = 1549817564128351331L;

    private Long id;
    private Long pid;

    private String customId;
    /**
     * 负责人
     */
    private String director;

    private Long tenantId;

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

    private Long height;

    private Long orgOrder;



    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 组织名拼音
     */
    private String orgPinyin;

    /**
     * 组织名多音字拼音
     */
    private String orgPinyin2;

    private Long userNum;

}
