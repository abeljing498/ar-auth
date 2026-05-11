package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import org.apache.ibatis.annotations.Param;

/**
 * @author guolanren
 */
public interface MdRoleMapper extends BaseMapper<MdRole> {

    /**
     * 查找同一个租户下角色名称相同
     * @param tenantId
     * @param roleName
     * @return
     */
    Integer getCount(@Param("tenantId") Long tenantId, @Param("appId") String appId, @Param("roleName") String roleName);

    /**
     *查找一个租户下id不相同，角色名称不相同
     * @param roleId
     * @param roleName
     * @return
     */
    Integer getCountRole(@Param("tenantId") Long tenantId,@Param("roleId") Long roleId,@Param("appId") String appId, @Param("roleName") String roleName);
}













