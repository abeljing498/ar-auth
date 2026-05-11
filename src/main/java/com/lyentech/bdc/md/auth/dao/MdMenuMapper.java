package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.MdMenu;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author guolanren
 */
public interface MdMenuMapper {

    Set<MdMenu> getByRoleId(@Param("roleId") Long roleId);

    Set<MdMenu> getByAppId(@Param("appId") String appId, @Param("opened") Integer opened);
}
