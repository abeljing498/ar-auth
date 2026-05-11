package com.lyentech.bdc.md.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.RoleOperationType;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.RoleByAuthParam;
import com.lyentech.bdc.md.auth.model.param.RoleUserParam;
import com.lyentech.bdc.md.auth.model.param.UserTenantRoleParam;
import com.lyentech.bdc.md.auth.model.vo.MdUserVO;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.model.vo.RoleByAuthVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import com.lyentech.bdc.md.auth.service.UserTenantRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthTokenConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-08-11
 */
@Service
public class UserTenantRoleServiceImpl extends ServiceImpl<UserTenantRoleMapper, UserTenantRole> implements UserTenantRoleService {

    @Resource
    private MdRoleMapper roleMapper;
    @Resource
    private UserTenantRoleMapper userTenantRoleMapper;
    @Autowired
    private UserTenantRoleService userTenantRoleService;
    @Resource
    private RoleAuthMapper roleAuthMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MdBlackUserMapper mdBlackUserMapper;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;
    @Resource
    private TenantRoleMapper tenantRoleMapper;
    @Autowired
    MdRoleOperationLogMapper mdRoleOperationLogMapper;

    @Override
    public Set<String> getRoleNames(String appId, Long tenantId, Long userId) {

        //获取用户在租户下的角色
        List<UserTenantRole> userTenantRoles = baseMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                .select(UserTenantRole::getRoleId)
                .eq(UserTenantRole::getAppId, appId)
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getUserId, userId));
        List<Long> roleIds = userTenantRoles.stream().map(UserTenantRole::getRoleId).collect(Collectors.toList());
        Set<String> roleNames = new HashSet<>();
        if (CollectionUtil.isNotEmpty(roleIds)) {
            List<MdRole> mdRoles = roleMapper.selectList(Wrappers.<MdRole>lambdaQuery()
                    .select(MdRole::getName)
                    .eq(MdRole::getStatus, 1)
                    .in(MdRole::getId, roleIds));
            roleNames = mdRoles.stream().map(MdRole::getName).collect(Collectors.toSet());
        }

        return roleNames;
    }

    @Override
    public List<Long> getRoleIds(String appId, Long tenantId, Long userId) {
        List<UserTenantRole> userTenantRoles = baseMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                .select(UserTenantRole::getRoleId)
                .eq(UserTenantRole::getAppId, appId)
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getUserId, userId));
        List<Long> roleIds = userTenantRoles.stream().map(UserTenantRole::getRoleId).distinct().collect(Collectors.toList());
        return roleIds;
    }

    @Override
    public Map getRoleInfoNames(String appId, Long tenantId, Long userId) {

        //获取用户在租户下的角色
        List<UserTenantRole> userTenantRoles = baseMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                .select(UserTenantRole::getRoleId)
                .eq(UserTenantRole::getAppId, appId)
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getUserId, userId));
        List<Long> roleIds = userTenantRoles.stream().map(UserTenantRole::getRoleId).distinct().collect(Collectors.toList());
        Set<String> roleNames = new HashSet<>();
        if (CollectionUtil.isNotEmpty(roleIds)) {
            List<MdRole> mdRoles = roleMapper.selectList(Wrappers.<MdRole>lambdaQuery()
                    .select(MdRole::getName)
                    .eq(MdRole::getStatus, 1)
                    .in(MdRole::getId, roleIds));
            roleNames = mdRoles.stream().map(MdRole::getName).collect(Collectors.toSet());
        }
        Map roles = new HashMap();
        roles.put("roleNames", roleNames);
        roles.put("roleIds", roleIds);
        return roles;
    }

    /**
     * 添加 人员租户角色 关联关系
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserTenantRole(UserTenantRoleParam userTenantRoleParam) {
        if (ObjectUtils.isEmpty(userTenantRoleParam)) {
            return;
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getUserId())) {
            throw new NullPointerException("用户ID不能为空");
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getTenantId())) {
            throw new NullPointerException("租户ID不能为空");
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getAppId())) {
            throw new NullPointerException("项目ID不能为空");
        }
        //判断添加的用户是否有角色，若有角色则进行添加
        List<UserTenantRole> userTenantRoles = userTenantRoleMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getUserId, userTenantRoleParam.getUserId())
                .eq(UserTenantRole::getTenantId, userTenantRoleParam.getTenantId())
                .eq(UserTenantRole::getAppId, userTenantRoleParam.getAppId()));
        List<Long> existRoleIds = userTenantRoles.stream().map(UserTenantRole::getRoleId).collect(Collectors.toList());
        if (!existRoleIds.equals(userTenantRoleParam.getRoleList())) {
            userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery()
                    .eq(UserTenantRole::getUserId, userTenantRoleParam.getUserId())
                    .eq(UserTenantRole::getTenantId, userTenantRoleParam.getTenantId())
                    .eq(UserTenantRole::getAppId, userTenantRoleParam.getAppId()));
            if (!CollectionUtils.isEmpty(userTenantRoleParam.getRoleList())) {
                //对同一个用户进行角色覆盖，先删除租户下用户的角色，再添加该用户的角色
                List<UserTenantRole> userTenantRoleList = new ArrayList<>();
                for (Long roleId : userTenantRoleParam.getRoleList()) {
                    UserTenantRole userTenantRole = new UserTenantRole();
                    userTenantRole.setRoleId(roleId);
                    userTenantRole.setUserId(userTenantRoleParam.getUserId());
                    userTenantRole.setTenantId(userTenantRoleParam.getTenantId());
                    userTenantRole.setAppId(userTenantRoleParam.getAppId());
                    userTenantRoleList.add(userTenantRole);
                }
                userTenantRoleService.saveBatch(userTenantRoleList);
            }
            StringBuilder stringBuilder = new StringBuilder();
            String authKey = stringBuilder.append(USER_CHANGE_PATH)
                    .append(userTenantRoleParam.getUserId()).append(":")
                    .append("client:").append(userTenantRoleParam.getAppId()).toString();
            stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userTenantRoleParam.getTenantId()), 24, TimeUnit.HOURS);
            //发送消息至下游客户端
            PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
            pushAppOrderMsgDto.setAppKey(userTenantRoleParam.getAppId());
            pushAppOrderMsgDto.setTenantId(userTenantRoleParam.getTenantId().toString());
            pushAppOrderMsgDto.setUserId(userTenantRoleParam.getUserId().toString());
            pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
            sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
        }
    }

    /**
     * 根据roleId,userId,tenantId删除角色下的用户
     *
     * @param roleId
     * @param userId
     * @param tenantId
     */
    @Override
    public void deleteUserRole(Long roleId, Long userId, Long tenantId) {
        UserTenantRole userTenantRole = userTenantRoleMapper.selectOne(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getRoleId, roleId)
                .eq(UserTenantRole::getUserId, userId));
        userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getRoleId, roleId)
                .eq(UserTenantRole::getUserId, userId));
        StringBuilder stringBuilder = new StringBuilder();
        String authKey = stringBuilder.append(USER_CHANGE_PATH)
                .append(userId).append(":")
                .append("client:").append(userTenantRole.getAppId()).toString();
        stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userTenantRole.getTenantId()), 24, TimeUnit.HOURS);
        //发送用户权限变更至下游
        PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
        pushAppOrderMsgDto.setAppKey(userTenantRole.getAppId());
        pushAppOrderMsgDto.setTenantId(userTenantRole.getTenantId().toString());
        pushAppOrderMsgDto.setUserId(userId.toString());
        pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
        sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
    }

    /**
     * 角色下批量添加成员
     *
     * @param roleUserList
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserList(List<RoleUserParam> roleUserList) {
        //添加人员租户角色关联表关系
        if (!ObjectUtils.isEmpty(roleUserList)) {
            for (RoleUserParam userRole : roleUserList) {
                UserTenantRole existUser = userTenantRoleMapper.selectOne(Wrappers.<UserTenantRole>lambdaQuery()
                        .eq(UserTenantRole::getTenantId, userRole.getTenantId())
                        .eq(UserTenantRole::getRoleId, userRole.getRoleId())
                        .eq(UserTenantRole::getUserId, userRole.getUserId())
                        .eq(UserTenantRole::getAppId, userRole.getAppId()));
                userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery()
                        .eq(UserTenantRole::getTenantId, userRole.getTenantId())
                        .eq(UserTenantRole::getRoleId, userRole.getRoleId())
                        .eq(UserTenantRole::getUserId, userRole.getUserId())
                        .eq(UserTenantRole::getAppId, userRole.getAppId()));
                if (ObjectUtils.isEmpty(existUser)) {
                    MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
                    mdOrgOperationLog.setOperationUserId(userRole.getOperateUserId());
                    mdOrgOperationLog.setNotes(JSON.toJSONString(userRole));
                    mdOrgOperationLog.setOperationUserName(userRole.getOperateUserName());
                    mdOrgOperationLog.setAppId(userRole.getAppId());
                    mdOrgOperationLog.setOperationType(RoleOperationType.USER_CHANGE.getCode());
                    mdOrgOperationLog.setRoleId(userRole.getRoleId());
                    mdOrgOperationLog.setUserIp(userRole.getUserIp());
                    mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
                    mdOrgOperationLog.setTenantId(userRole.getTenantId());
                    mdOrgOperationLog.setCreateTime(new Date());
                    mdRoleOperationLogMapper.insert(mdOrgOperationLog);
                    StringBuilder stringBuilder = new StringBuilder();
                    String authKey = stringBuilder.append(USER_CHANGE_PATH)
                            .append(userRole.getUserId()).append(":")
                            .append("client:").append(userRole.getAppId()).toString();
                    stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userRole.getTenantId()), 24, TimeUnit.HOURS);
                    //发送用户权限变更至下游
                    PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
                    pushAppOrderMsgDto.setAppKey(userRole.getAppId());
                    pushAppOrderMsgDto.setTenantId(userRole.getTenantId().toString());
                    pushAppOrderMsgDto.setUserId(userRole.getUserId().toString());
                    pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
                    sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
                }
            }
        }
        userTenantRoleMapper.insertList(roleUserList);
    }

    /**
     * 获取角色下成员列表
     *
     * @param pageNum
     * @param pageSize
     * @param roleId
     * @param nickname
     * @return
     */
    @Override
    public PageResult<MdUserVO> selectUserByRoleId(Long pageNum, Long pageSize, Long roleId, String nickname) {
        if (ObjectUtils.isEmpty(roleId)) {
            throw new NullPointerException("角色ID不可为空");
        }
        IPage<MdUserVO> iPage = userTenantRoleMapper.getUserByRoleId(new Page<>(pageNum, pageSize), roleId, nickname);
        PageResult<MdUserVO> pageResult = PageResult.build(pageNum, pageSize, iPage.getPages(), iPage.getTotal(), iPage.getRecords());
        return pageResult;
    }

    /**
     * 角色下添加人员（添加人员角色关联关系）
     *
     * @param roleUserParam
     */
    @Override
    public void addUserRole(RoleUserParam roleUserParam) {
        if (ObjectUtils.isEmpty(roleUserParam.getUserId())) {
            throw new IllegalParamException("用户ID不能为空");
        }
        if (ObjectUtils.isEmpty(roleUserParam.getRoleId())) {
            throw new IllegalParamException("角色ID不能为空");
        }
        if (ObjectUtils.isEmpty(roleUserParam.getTenantId())) {
            throw new IllegalParamException("租户ID不能为空");
        }
        if (ObjectUtils.isEmpty(roleUserParam.getAppId())) {
            throw new IllegalParamException("项目ID不能为空");
        }
        UserTenantRole userTenantRole = new UserTenantRole();
        UserTenantRole existUser = userTenantRoleMapper.selectOne(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getTenantId, roleUserParam.getTenantId())
                .eq(UserTenantRole::getRoleId, roleUserParam.getRoleId())
                .eq(UserTenantRole::getUserId, roleUserParam.getUserId())
                .eq(UserTenantRole::getAppId, roleUserParam.getAppId()));
        if (ObjectUtils.isEmpty(existUser)) {
            userTenantRole.setRoleId(roleUserParam.getRoleId());
            userTenantRole.setUserId(roleUserParam.getUserId());
            userTenantRole.setAppId(roleUserParam.getAppId());
            userTenantRole.setTenantId(roleUserParam.getTenantId());
            userTenantRoleMapper.insert(userTenantRole);
            StringBuilder stringBuilder = new StringBuilder();
            String authKey = stringBuilder.append(USER_CHANGE_PATH)
                    .append(roleUserParam.getUserId()).append(":")
                    .append("client:").append(roleUserParam.getAppId()).toString();
            stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(roleUserParam.getTenantId()), 24, TimeUnit.HOURS);
            //发送用户权限变更至下游
            PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
            pushAppOrderMsgDto.setAppKey(roleUserParam.getAppId());
            pushAppOrderMsgDto.setTenantId(roleUserParam.getTenantId().toString());
            pushAppOrderMsgDto.setUserId(roleUserParam.getUserId().toString());
            pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
            sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
        }

    }

    /**
     * 删除用户下的所有角色
     *
     * @param userTenantRoleParam
     */
    @Override
    public void deleteUserRole(UserTenantRoleParam userTenantRoleParam) {
        if (ObjectUtils.isEmpty(userTenantRoleParam)) {
            return;
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getUserId())) {
            throw new IllegalParamException("用户ID不能为空");
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getTenantId())) {
            throw new IllegalParamException("租户ID不能为空");
        }
        if (ObjectUtils.isEmpty(userTenantRoleParam.getAppId())) {
            throw new IllegalParamException("项目ID不能为空");
        }
        userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getUserId, userTenantRoleParam.getUserId())
                .eq(UserTenantRole::getTenantId, userTenantRoleParam.getTenantId())
                .eq(UserTenantRole::getAppId, userTenantRoleParam.getAppId()));
        StringBuilder stringBuilder = new StringBuilder();
        String authKey = stringBuilder.append(USER_CHANGE_PATH)
                .append(userTenantRoleParam.getUserId()).append(":")
                .append("client:").append(userTenantRoleParam.getAppId()).toString();
        stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userTenantRoleParam.getTenantId()), 24, TimeUnit.HOURS);
        //发送用户权限变更至下游
        PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
        pushAppOrderMsgDto.setAppKey(userTenantRoleParam.getAppId());
        pushAppOrderMsgDto.setTenantId(userTenantRoleParam.getTenantId().toString());
        pushAppOrderMsgDto.setUserId(userTenantRoleParam.getUserId().toString());
        pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
        sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
    }

    @Override
    public RoleByAuthVO getUserNumByAuth(RoleByAuthParam roleByAuthParam) {
        if (ObjectUtils.isEmpty(roleByAuthParam.getRoleId())) {
            throw new IllegalParamException("角色id不可为空");
        }
        if (ObjectUtils.isEmpty(roleByAuthParam.getIsMax())) {
            throw new IllegalParamException("参数错误");
        }
        RoleByAuthVO roleByAuthVO = new RoleByAuthVO();
        MdRole mdRole = roleMapper.selectById(roleByAuthParam.getRoleId());
        //判断是否是公司中的最大角色权
        if (roleByAuthParam.getIsMax().equals(true)) {
            //是最大权限集合，判断需要修改的具体涉及的权限有哪些，所以authIds不可为空
            if (ObjectUtils.isEmpty(roleByAuthParam.getAuthIds())) {
                throw new IllegalParamException("涉及的权限id不可为空");
            }
            //获取最大角色的相关信息
            List<MdBlackUser> userList = mdBlackUserMapper.selectList(Wrappers.<MdBlackUser>lambdaQuery()
                    .eq(MdBlackUser::getAppId, mdRole.getAppId()));
            List<Long> blackUserList = userList.stream().map(MdBlackUser::getUserId).distinct().collect(Collectors.toList());
            //获取该租户下其他角色列表
            List<TenantRole> userTenantRoles = tenantRoleMapper.selectList(Wrappers.<TenantRole>lambdaQuery()
                    .eq(TenantRole::getTenantId, roleByAuthParam.getTenantId())
                    .eq(TenantRole::getAppId, mdRole.getAppId()));
            List<Long> existRoleIds = userTenantRoles.stream().map(TenantRole::getRoleId).distinct().collect(Collectors.toList());
            if (ObjectUtils.isEmpty(existRoleIds)) {
                //判断角色列表如果为空
                roleByAuthVO.setRoleNum(0);
                roleByAuthVO.setUserNum(0);
            } else {
                List<Long> roleByAuths = new ArrayList<>();

                Set<Long> roleChangeIds = new HashSet<>();
                for (Long id : existRoleIds) {
                    //获取每个角色绑定的权限id集合
                    List<Long> roleAuths = roleAuthMapper.getRoleId(id, roleByAuthParam.getTenantId());
//                    List<Long> idCollect = roleAuths.stream().map(RoleAuth::getAuthId).collect(Collectors.toList());
                    for (Long authId : roleByAuthParam.getAuthIds()) {
                        //判断该角色的权限id是否有需要变更的权限
                        if (roleAuths.contains(authId)) {
                            roleChangeIds.add(id);
                            roleByAuths.add(id);
                            if (roleByAuthParam.getIsDelete().equals(true)) {
                                //删除对应权限id
                                roleAuthMapper.delete(Wrappers.<RoleAuth>lambdaQuery()
                                        .eq(RoleAuth::getAuthId, authId)
                                        .eq(RoleAuth::getRoleId, id));
                                roleByAuthVO.setIsDelete(true);
                            } else {
                                roleByAuthVO.setIsDelete(false);
                            }
                        }
                    }
                }
                roleByAuthVO.setRoleNum(roleChangeIds.size());
                Set<Long> userIds = new HashSet<>();
                for (Long id : roleByAuths) {
                    List<Long> userNumByRoleId = userTenantRoleMapper.getUserNumByRoleId(id);
                    userIds.addAll(userNumByRoleId);
                }

                if (roleByAuthParam.getIsDelete().equals(true)) {
                    //将被删除权限的id添加到权限变更类
                    for (Long userId : userIds) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String authKey = stringBuilder.append(USER_CHANGE_PATH)
                                .append(userId).append(":")
                                .append("client:").append(mdRole.getAppId()).toString();
                        stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(roleByAuthParam.getTenantId()), 24, TimeUnit.HOURS);
                    }
                }
                List<Long> finalList = userIds.parallelStream()
                        .filter(x -> !blackUserList.contains(x)).collect(Collectors.toList());
                roleByAuthVO.setUserNum(finalList.size());
                roleByAuthVO.setIsMax(true);
            }
        } else {
            List<Long> userNumByRoleId = userTenantRoleMapper.getUserNumByRoleId(roleByAuthParam.getRoleId());
            List<MdBlackUser> userList = mdBlackUserMapper.selectList(Wrappers.<MdBlackUser>lambdaQuery()
                    .eq(MdBlackUser::getAppId, mdRole.getAppId()));
            List<Long> blackUserList = userList.stream().map(MdBlackUser::getUserId).distinct().collect(Collectors.toList());
            roleByAuthVO.setRoleId(roleByAuthParam.getRoleId());
            List<Long> finalList = userNumByRoleId.parallelStream()
                    .filter(x -> !blackUserList.contains(x)).collect(Collectors.toList());
            roleByAuthVO.setUserNum(finalList.size());
            roleByAuthVO.setIsMax(false);
            roleByAuthVO.setRoleNum(0);
        }
        //发送用户权限变更至下游
        PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
        pushAppOrderMsgDto.setAppKey(mdRole.getAppId());
        pushAppOrderMsgDto.setTenantId(roleByAuthParam.getTenantId().toString());
        pushAppOrderMsgDto.setRoleId(roleByAuthParam.getRoleId().toString());
        pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
        sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
        return roleByAuthVO;
    }

    /**
     * 移除角色用户并记录日志
     * @param userRole
     */
    @Override
    public void deleteRoleUser(RoleUserParam userRole) {
        MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
        mdOrgOperationLog.setOperationUserId(userRole.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(userRole));
        mdOrgOperationLog.setOperationUserName(userRole.getOperateUserName());
        mdOrgOperationLog.setAppId(userRole.getAppId());
        mdOrgOperationLog.setOperationType(RoleOperationType.USER_CHANGE.getCode());
        mdOrgOperationLog.setRoleId(userRole.getRoleId());
        mdOrgOperationLog.setUserIp(userRole.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(userRole.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        mdRoleOperationLogMapper.insert(mdOrgOperationLog);
        deleteUserRole(userRole.getRoleId(), userRole.getUserId(), userRole.getTenantId());

    }

    @Override
    public List<RoleVO> getRoleListByTenantAndUserId(Long tenantId, Long id, String appId) {
        return userTenantRoleMapper.getRoleListByTenantAndUserId(tenantId,id,appId);
    }
}

