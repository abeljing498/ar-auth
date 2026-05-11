package com.lyentech.bdc.md.auth.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.lang.annotation.Target;

/**
 * @Author :yan
 * @Date :Create in 2022/5/25
 * @Description :
 */

@Data
public class TenantVO implements Serializable {

    private static final long serialVersionUID = -4302615774205341296L;

    private Long id;

    /**
     * 顶层组织id
     */
    private Long orgId;

    /**
     * 租户名
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否有导出数据的权限
     */
    private Boolean auth;
}
