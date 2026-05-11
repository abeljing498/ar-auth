package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.vo.RoleUserVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;
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
public interface TenantRoleMapper extends BaseMapper<TenantRole> {

    void insertList(@Param("roleUserList") List<RoleUserVO> roleUserList);

    /**
     * 分页获取租户下所有角色信息
     * @param page
     * @param appId
     * @param roleName
     * @return
     */
    IPage<RoleVO> getRoleListByAppId(Page<RoleVO> page, @Param("appId") String appId,
                                     @Param("tenantId") Long tenantId , @Param("roleName") String roleName);

    /**
     * 获取租户下所有角色
     * @param tenantId
     * @return
     */
    List<MdRole>getRoleListByTenantId(@Param("tenantId") Long tenantId, @Param("appId") String appId);
}
