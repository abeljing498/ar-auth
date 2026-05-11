package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 8:52
 */
@Data
@TableName("md_black_log")
public class MdBlackLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("reason")
    private String reason;

    @TableField("app_id")
    private String appId;

    @TableField("user_id")
    private Long userId;

    @TableField("create_time")
    private Date createTime;

    @TableField("operator_id")
    private Long oid;

    @TableField("black_id")
    private Long bid;

    @TableField("operator_nickname")
    private String operName;

    @TableField("type")
    private String type;
    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;
}
