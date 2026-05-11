package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.vo.RoleDetailVO;

/**
 * @author guolanren
 */
public interface MdRoleService extends IService<MdRole> {
    /**
     * 新增角色
     * @param roleParam 角色
     */
    Long add(RoleParam roleParam);

    /**
     * 删除角色
     * @param roleId 角色Id
     */
    void delete(Long roleId) throws InterruptedException, Exception;

    /**
     * 修改角色信息
     * @param roleParam 角色信息
     */
    void update(RoleParam roleParam);

    /**
     * 角色详情
     * @param roleId
     * @return
     */
    RoleDetailVO getRoleDetail(Long roleId);

    /**
     * 改变角色状态
     */
    void changeStatus(Long id, Boolean status);

    /**
     * 项目添加租户时 默认 对项目关联的租户添加一个管理员角色
     */
    void addAdminRole(Long tenantId, String appId, Long userId);

    void authChange(Long authId, String appId, String type, String isNotTips, String userId);

    void changeRoleStatus(RoleParam roleParam);

    void deleteAppRole(RoleParam roleParam) throws Exception;
}
