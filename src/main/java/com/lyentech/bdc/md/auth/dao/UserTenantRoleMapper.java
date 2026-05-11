package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.UserTenantRole;
import com.lyentech.bdc.md.auth.model.param.RoleUserParam;
import com.lyentech.bdc.md.auth.model.vo.MdUserVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yan
 * @since 2022-08-11
 */
public interface UserTenantRoleMapper extends BaseMapper<UserTenantRole> {

    /**
     * 根据appId和租户id列表获取角色
     * @param tenantIds
     * @param appId
     * @return
     */
    Set<MdRole> getByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantIds") List<Long> tenantIds);

    /**
     * 插入用户租户角色关联表数据
     * @param roleUserList
     */
    void insertList(@Param("roleUserList") List<RoleUserParam> roleUserList);

    Set<MdRole> getByTenantIdAndRoleId(@Param("appId") String appId, @Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    /**
     * 查找角色下用户
     * @param page
     * @param roleId
     * @param nickname
     * @return
     */
    IPage<MdUserVO> getUserByRoleId(@Param("page") Page page,
                                    @Param("roleId") Long roleId,
                                    @Param("nickname") String nickname);

    List<Long> getUserNumByRoleId(@Param("roleId") Long roleId);
    List<RoleVO> getRoleListByUserId(@Param("appId") String appId, @Param("tenantId") Long tenantId, @Param("userId") Long userId);
    List<RoleVO>getRoleListByTenantAndUserId(@Param("tenantId") Long tenantId,@Param("userId") Long userId, @Param("appId") String appId);
}
