package com.lyentech.bdc.md.auth.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author guolanren
 */
public interface MdPermissionMapper {

    /**
     * 根据角色id获取权限
     * @param roleId
     * @return
     */
    Set<String> getByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据appId获取权限
     * @param appId
     * @return
     */
    Set<String> getByAppId(@Param("appId") String appId, @Param("opened") Integer opened);

    Set<Long> getByPermission(@Param("appId") String appId, @Param("path") String path,@Param("userId") Long userId);

}
