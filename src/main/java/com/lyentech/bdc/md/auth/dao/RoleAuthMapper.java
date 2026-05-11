package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.RoleAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface RoleAuthMapper extends BaseMapper<RoleAuth> {
    /**
     * //批量添加角色权限关联表
     * @param roleAuthList
     */
    void insertList(@Param("roleAuthList") List<RoleAuth> roleAuthList);

    List<Long> getRoleId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);
}
