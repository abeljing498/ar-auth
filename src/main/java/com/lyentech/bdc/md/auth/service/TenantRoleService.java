package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.vo.MdRoleVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface TenantRoleService extends IService<TenantRole> {
    /**
     * 分页查找租户下角色列表
     * @param appId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult<RoleVO> selectRoleList(String appId, Long tenantId , Long pageNum, Long pageSize, String roleName);

    /**
     * 获取租户下所有角色
     *
     * @param tenantId
     * @return
     */
    List<MdRoleVO> getRoleList(Long tenantId, String appId);

}
