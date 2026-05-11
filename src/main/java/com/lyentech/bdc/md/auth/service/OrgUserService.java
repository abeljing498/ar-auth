package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.*;
import com.lyentech.bdc.md.auth.model.vo.*;

import java.util.List;
import java.util.Map;


/**
 * @author YaoYulong
 */
public interface OrgUserService {
    /**
     * 在组织下添加人员
     *
     * @param orgUserParam
     */
    PasswordVO add(OrgUserParam orgUserParam) throws Exception;

    void addUserByEmail(OrgUserParam orgUserParam) throws Exception;

    PasswordVO addExternalUser(OrgUserParam orgUserParam) throws Exception;

    /**
     * 组织下对应的人员
     *
     * @param orgId 组织id
     * @param userState
     * @return
     */
    PageResult<UserDetailsVO> getUserList(Long pageNum, Long pageSize, Long tenantId, Long orgId, String account, String keyword, String appId, Integer userState);

    /**
     * 修改人员信息
     *
     * @param userParam
     */
    void update(OrgUserParam userParam) throws Exception;

    /**
     * 删除组织下人员
     *
     * @param userParamList
     */
    void delete(List<DeleteUserParam> userParamList);

    /**
     * 查看看人员详情
     *
     * @param id     人员Id
     * @param action
     * @return 人员信息
     */
    UserDetailsVO getUserDetail(Long id, Long orgId, Long tenantId, String appId, String action);

    /**
     * 外部接口：删除人员
     *
     * @param deleteUserParam 批量删除同一组织下的人员
     */
    void deleteUser(DeleteUserParam deleteUserParam);

    /**
     * 通过电话获取人员信息
     *
     * @param phone
     * @param tenantId
     * @param appId
     * @return
     */
    UserInfoVO getUserInfo(String phone, Long tenantId, String appId);

    /**
     * 删除单个用户
     * @param deleteUserParam
     */
    void deleteOrgUser(DeleteUserParam deleteUserParam);

    /**
     * 获取租户下所有用户
     * @param tenantId
     * @return
     */
    List<TenantUserVO> getUserByTenantId(Long tenantId, Long orgId, String keyword);

    UserVO userByPhone(String phone);

    /**
     * 获取人员信息集合
     * @param keys
     * @param type
     * @return
     */
    List<UserInfoVO> listUserDetails(List<String> keys, String type, Long tenantId, Long height);


    UserVO getUserInfoByAccount(String account);

    Map<String,Object> importTeamUser(ImportTeamUserParam param);

    List<OrgVO> selectOrgPath(Long tenantId, Long orgId, List<OrgVO> orgVOList);

    List<MdUser> saveUserData(ImportTeamUserParam param);

    Map<String,Object> getUserOrgAndRole(Long userId, Long tenantId, String appId, Long orgId);

    UserVO getUserById(Long id);

    MdUser addOutUserByAccount(OrgUserParam orgUserParam) throws Exception;

    MdUser outUserAdd(OutUserAddParam param) throws Exception;

    List<Map<String,Object>> getDeptLeaderByUserEmail(String email, Long tenantId);

    Long addHumanResourceOrg(OrgUserParam orgUserParam);

    Object exportUserLog(ExportUserParam param);

    void addSearchUserByEmail(OrgUserParam orgUserParam) throws Exception;

    MdUser customerUserAdd(OutUserAddParam param) throws Exception;

    Object saveFaqcToken(Map<String, Object> param);

    PageResult<UserAppRolesVO> getUserAppRoleList(Long pageNum, Long pageSize, String keyword);
}

