package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guolanren
 */
public interface MdUserMapper extends BaseMapper<MdUser> {

    /**
     * 根据手机号查询用户
     * @param phone
     * @return
     */
    MdUser searchByPhone(String phone);

    /**
     * 根据邮箱号查询用户
     * @param email
     * @return
     */
    MdUser searchByEmail(String email);

    /**
     * 根据账号查询用户
     * @param account
     * @return
     */
    MdUser searchByAccount(String account);

    MdUser searchByPhoneIncludeDeleted(String phone);

    void register(@Param("id") Long id, @Param("nickname") String nickname, @Param("password") String password);

    /**
     * 通过tenantId查找租户下所有用户
     *通过电话昵称模糊查找
     * @param page
     * @param tenantId
     * @param userState
     * @return
     */
    IPage<UserDetailsVO> searchUserList(Page page,
                                        @Param("keyword") String keyword,
                                        @Param("tenantId") Long tenantId,
                                        @Param("account") String account,
                                        @Param("orgId") Long orgId,
                                        @Param("appId") String appId,
                                        @Param("userState")Integer userState);

    /**
     * 通过组织集合查找组织下所有人员
     * @param page
     * @param orgIds
     * @return
     */
    IPage<UserDetailsVO> selectUserListByOrgId(Page<UserDetailsVO>page,
                                               @Param("orgIds") List<Long> orgIds,
                                               @Param("name") String name,
                                               @Param("phone") String phone);

    /**
     * 通过人员Id查找所有组织
     *
     * @param id
     * @return
     */
    List<OrgNewVO> getOrgNameByUserId(@Param("id") Long id, @Param("orgId") Long orgId, @Param("tenantId") Long tenantId);

    /**
     * 通过人员Id查找所属项目名称
     * @param id
     * @return
     */
    List<String> getAppNameByUserId(@Param("id")Long id);

    /**
     * 替换 searchUserList 方法（未使用selectUserList）
     * @param page
     * @param phone
     * @param name
     * @param tenantId
     * @param orgId
     * @param appId
     * @return
     */
    IPage<UserDetailsVO> selectUserList(Page page,
                                        @Param("phone") String phone,
                                        @Param("name") String name,
                                        @Param("tenantId") Long tenantId,
                                        @Param("orgId") Long orgId,
                                        @Param("appId")String appId);

    List<TenantUserVO> selectUserByTenantId(@Param("tenantId") Long tenantId,
                                            @Param("orgId") Long orgId,
                                            @Param("keyword") String keyword);

    UserInfoVO selectByKey(@Param("key") String key, @Param("type") String type, @Param("tenantId") Long tenantId);

    IPage<UserAppRolesVO> getUserAppRoleList(Page page, @Param("keyword") String keyword);
}
