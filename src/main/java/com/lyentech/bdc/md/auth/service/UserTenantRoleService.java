package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.UserTenantRole;
import com.lyentech.bdc.md.auth.model.param.RoleByAuthParam;
import com.lyentech.bdc.md.auth.model.param.RoleUserParam;
import com.lyentech.bdc.md.auth.model.param.UserTenantRoleParam;
import com.lyentech.bdc.md.auth.model.vo.MdUserVO;
import com.lyentech.bdc.md.auth.model.vo.RoleByAuthVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yan
 * @since 2022-08-11
 */
public interface UserTenantRoleService extends IService<UserTenantRole> {
    /**
     * 用户下新增角色（新增用户租户角色关联关系）
     * @param userTenantRoleParam
     */
    void addUserTenantRole(UserTenantRoleParam userTenantRoleParam);

    /**
     * 根据参数获取用户在租户下的角色名列表
     * @param tenantId
     * @param userId
     * @param appId
     * @return
     */
    Set<String> getRoleNames(String appId, Long tenantId, Long userId);

    List<Long> getRoleIds(String appId, Long tenantId, Long userId);

    Map getRoleInfoNames(String appId, Long tenantId, Long userId);
    /**
     * 根据roleId,userId,tenantId删除角色下的用户
     * @param roleId
     * @param userId
     * @param tenantId
     */
    void deleteUserRole(Long roleId, Long userId, Long tenantId);

    /**
     * 角色下批量添加成员
     * @param roleUserList
     */
    void addUserList(List<RoleUserParam> roleUserList);

    /**
     * 角色下添加人员(添加角色人员关系)
     * @param roleUserParam
     */
    void addUserRole(RoleUserParam roleUserParam);

    /**
     *获取角色下成员列表
     * @param roleId
     * @return
     */
    PageResult<MdUserVO> selectUserByRoleId(Long pageNum, Long pageSize , Long roleId, String nickname);

    /**
     * 删除用户下的角色（删除用户租户角色关联关系）
     * @param userTenantRoleParam
     */
    void deleteUserRole(UserTenantRoleParam userTenantRoleParam);

    RoleByAuthVO getUserNumByAuth(RoleByAuthParam roleByAuthParam);

    void deleteRoleUser(RoleUserParam roleUserParam);

    List<RoleVO> getRoleListByTenantAndUserId(Long tenantId, Long userId, String appId);
}
