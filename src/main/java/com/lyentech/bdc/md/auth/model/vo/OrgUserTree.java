package com.lyentech.bdc.md.auth.model.vo;

import com.lyentech.bdc.util.BasicTree;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrgUserTree extends BasicTree implements Serializable {

    private static final long serialVersionUID = 1549817564128351331L;

    private Long id;
    private Long pid;

    private String customId;
    private List<? extends BasicTree> subNode;

    private Long tenantId;


    /**
     * 组织名
     */
    private String name;

    private Long height;



    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 组织名拼音
     */
    private String orgPinyin;

    /**
     * 组织名多音字拼音
     */
    private String orgPinyin2;

    private List<OrgUserVO> userList;
}
