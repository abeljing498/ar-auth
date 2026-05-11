package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

/**
 * @author YuYi
 * @create 2023/4/1
 * @create 17:08
 */
@Data
public class RoleByAuthVO {
    Long roleId;

    /**
     * 获取受影响的用户人数
     */
    Integer userNum;

    /**
     * 获取受影响的角色人数
     */
    Integer roleNum;

    /**
     * 是否是最大角色
     */
    Boolean isMax;

    Boolean isDelete;
}
