package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.MdRoleTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.RoleOperationType;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.vo.*;
import com.lyentech.bdc.md.auth.service.AuthService;
import com.lyentech.bdc.md.auth.service.MdRoleService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.ADMIN;
import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.ANONYMOUS;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;

/**
 * @author guolanren
 */
@Service
public class MdRoleServiceImpl extends ServiceImpl<MdRoleMapper, MdRole> implements MdRoleService {

    @Resource
    private MdRoleMapper roleMapper;
    @Resource
    private RoleAuthMapper roleAuthMapper;
    @Resource
    private TenantRoleMapper tenantRoleMapper;
    @Resource
    private AuthMapper authMapper;
    @Resource
    private MdUserMapper mdUserMapper;
    @Resource
    private UserTenantRoleMapper userTenantRoleMapper;
    @Resource
    private AuthService authService;
    @Resource
    MdAppMapper mdAppMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    SendMessageToWebSocketService sendMessageToWebSocketService;
    @Autowired
    MdRoleOperationLogMapper mdRoleOperationLogMapper;

    /**
     * 新增角色
     *
     * @param roleParam 角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(RoleParam roleParam) {
        Integer count = roleMapper.getCount(roleParam.getTenantId(), roleParam.getAppId(), roleParam.getName());
        if (count > 0) {
            throw new IllegalParamException("该角色名称已存在，请检查后再次提交！");
        }
        Long id = null;
        MdRole mdRole = new MdRole();
        mdRole.setName(roleParam.getName());
        mdRole.setDescription(roleParam.getDescription());
        mdRole.setAppId(roleParam.getAppId());
        mdRole.setType(MdRoleTypeConstant.OTHER_ROLE_TYPE);
        mdRole.setStatus(roleParam.getStatus());
        mdRole.setCreateBy(roleParam.getCreateBy());
        roleMapper.insert(mdRole);
        //添加角色权限关联表关系
        RoleAuth roleAuth = new RoleAuth();
        roleAuth.setRoleId(mdRole.getId());
        for (Long authId : roleParam.getAuthList()) {
            roleAuth.setAuthId(authId);
            roleAuthMapper.insert(roleAuth);
        }
        //添加App、租户、角色关联表关系
        TenantRole tenantRole = new TenantRole();
        tenantRole.setAppId(roleParam.getAppId());
        tenantRole.setRoleId(mdRole.getId());
        tenantRole.setTenantId(roleParam.getTenantId());
        tenantRoleMapper.insert(tenantRole);
        id = mdRole.getId();
        MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
        mdOrgOperationLog.setOperationUserId(roleParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(roleParam));
        mdOrgOperationLog.setOperationUserName(roleParam.getOperateUserName());
        mdOrgOperationLog.setAppId(roleParam.getAppId());
        mdOrgOperationLog.setOperationType(RoleOperationType.ADD.getCode());
        mdOrgOperationLog.setRoleId(id);
        mdOrgOperationLog.setUserIp(roleParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(roleParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        mdRoleOperationLogMapper.insert(mdOrgOperationLog);
        return id;
    }

    /**
     * 删除角色
     *
     * @param roleId 角色Id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) throws Exception {
        Long id = roleId;
        TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, roleId));
        if (ObjectUtils.isNotEmpty(tenantRole)) {
            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setRoleId(String.valueOf(roleId));
            pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
            pushMessageDto.setStatus("DELETE");
            pushMessageDto.setAppKey(tenantRole.getAppId());
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
        }
        //这里休眠时为了让下游有足够的时间消费通知用户权限变更，这里提前发送消息时因为如果删除权限绑定关系后，下游系统没法查出来通知到
        Thread.sleep(500);
        //删除角色表
        roleMapper.deleteById(id);
        //取消角色权限关联表关系
        roleAuthMapper.delete(Wrappers.<RoleAuth>lambdaQuery().eq(RoleAuth::getRoleId, id));
        //删除用户角色关系
        userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery().eq(UserTenantRole::getRoleId, roleId));
        //删除组织角色关联表
        tenantRoleMapper.delete(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, id));
        List<UserTenantRole> userTenantRoles = userTenantRoleMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery().eq(UserTenantRole::getRoleId, roleId));
        if (ObjectUtils.isNotEmpty(userTenantRoles)) {
            for (UserTenantRole userTenantRole : userTenantRoles) {
                StringBuilder stringBuilder = new StringBuilder();
                String authKey = stringBuilder.append(USER_CHANGE_PATH)
                        .append(userTenantRole.getUserId()).append(":")
                        .append("client:").append(userTenantRole.getAppId()).toString();
                stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userTenantRole.getTenantId()));
            }
        }
    }

    /**
     * 修改角色信息
     *
     * @param roleParam 角色信息
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleParam roleParam) {
        MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
        mdOrgOperationLog.setOperationUserId(roleParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(roleParam));
        mdOrgOperationLog.setOperationUserName(roleParam.getOperateUserName());
        mdOrgOperationLog.setAppId(roleParam.getAppId());
        mdOrgOperationLog.setOperationType(RoleOperationType.UPDATE.getCode());
        mdOrgOperationLog.setRoleId(roleParam.getId());
        mdOrgOperationLog.setUserIp(roleParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(roleParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        mdRoleOperationLogMapper.insert(mdOrgOperationLog);
        //删除编辑前的所有权限
        //同一个租户下角色名称不相同，id不相同角色名不相同
        Integer count = roleMapper.getCountRole(roleParam.getTenantId(),
                roleParam.getId(), roleParam.getAppId(), roleParam.getName());
        if (count > 0) {
            throw new IllegalParamException("该角色名称已存在，请检查后再次提交！");
        }
        MdRole mdRole = new MdRole();
        BeanUtils.copyProperties(roleParam, mdRole);
        List<Long> addAuthIds = new ArrayList<>();
        //删除角色下权限
        //先查一下权限是否有变更
        List<RoleAuth> roleAuths = roleAuthMapper.selectList(Wrappers.<RoleAuth>lambdaQuery().eq(RoleAuth::getRoleId, roleParam.getId()));
        List<Long> existAuthIds = roleAuths.stream().map(RoleAuth::getAuthId).collect(Collectors.toList());

        roleAuthMapper.delete(Wrappers.<RoleAuth>lambdaQuery().eq(RoleAuth::getRoleId, roleParam.getId()));
        //添加修改后的角色权限
        List<RoleAuth> roleAuthList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(roleParam.getAuthList())) {
            for (Long authId : roleParam.getAuthList()) {
                RoleAuth roleAuth = new RoleAuth();
                roleAuth.setAuthId(authId);
                roleAuth.setRoleId(roleParam.getId());
                roleAuthList.add(roleAuth);
            }
            if (!ObjectUtils.isEmpty(roleAuthList)) {
                roleAuthMapper.insertList(roleAuthList);
            }
        }
        roleMapper.updateById(mdRole);
        addAuthIds = roleAuthList.stream().map(RoleAuth::getAuthId).collect(Collectors.toList());
        if (!existAuthIds.equals(addAuthIds)) {
            List<UserTenantRole> userTenantRoles = userTenantRoleMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery().eq(UserTenantRole::getRoleId, mdRole.getId()));
            if (ObjectUtils.isNotEmpty(userTenantRoles)) {
                for (UserTenantRole userTenantRole : userTenantRoles) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String authKey = stringBuilder.append(USER_CHANGE_PATH)
                            .append(userTenantRole.getUserId()).append(":")
                            .append("client:").append(userTenantRole.getAppId()).toString();
                    stringRedisTemplate.boundValueOps(authKey).set(String.valueOf(userTenantRole.getTenantId()));
                }
            }

        }
    }

    /**
     * 角色详情
     *
     * @param roleId
     * @return
     */
    @Override
    public RoleDetailVO getRoleDetail(Long roleId) {
        MdRole mdRole = roleMapper.selectById(roleId);
        RoleDetailVO roleVo = new RoleDetailVO();
        if (mdRole == null) {
            return roleVo;
        }
        roleVo.setId(mdRole.getId());
        roleVo.setName(mdRole.getName());
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().select(MdUser::getNickname).eq(MdUser::getId, mdRole.getCreateBy()));
        if (ObjectUtils.isNotEmpty(mdUser)) {
            roleVo.setCreator(mdUser.getNickname());
            roleVo.setStatus(mdRole.getStatus());
            roleVo.setCreateTime(mdRole.getCreateTime());
        }

        List<Long> authList = authMapper.getAuthId(roleId);
        List<Long> strList = new ArrayList<>();
        for (Long authId : authList) {
            strList.add(authId);
        }
        roleVo.setAuthList(strList);
        roleVo.setDescription(mdRole.getDescription());
        List<AuthVO> authTreeList = authMapper.getAuthTree(roleId);
        if (ObjectUtils.isNotEmpty(authTreeList)) {
            for (AuthVO authVO : authTreeList) {
                int size = authVO.getGroupLists().size();
                authVO.setNum(size);
                int num = authMapper.getAuthNum(authVO.getGroupId());
                if (size == num) {
                    authVO.setIsAll(true);
                } else {
                    authVO.setIsAll(false);
                }
            }
        }
        roleVo.setAuthTreeList(authTreeList);
        return roleVo;
    }

    /**
     * 改变角色状态
     *
     * @param id
     * @param status
     */
    @Override
    public void changeStatus(Long id, Boolean status) {
        MdRole mdRole = new MdRole();
        mdRole.setId(id);
        mdRole.setStatus(status);
        roleMapper.updateById(mdRole);
        TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, id));
        if (ObjectUtils.isNotEmpty(tenantRole)) {
            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setRoleId(String.valueOf(id));
            pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
            if (status.equals(true)) {
                pushMessageDto.setStatus("ADD");
            } else {
                pushMessageDto.setStatus("DELETE");
            }
            pushMessageDto.setAppKey(tenantRole.getAppId());
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
        }
    }

    @Override
    public void addAdminRole(Long tenantId, String appKey, Long userId) {
        //获取项目所有权限
        List<AuthOldVO> authOldVOList = authService.getAllAuthOld(appKey);
        //配置所有权限给角色
        RoleParam roleParam = new RoleParam();
        List<Long> authIds = new LinkedList<>();
        for (AuthOldVO authOldVO : authOldVOList) {
            authIds.add(authOldVO.getId());
        }
        //获取创建项目 人员ID
//        Long createId = mdAppMapper.getCreateName(appKey);
        //生成租户下管理员角色
        roleParam.setAuthList(authIds);
        roleParam.setAppId(appKey);
        roleParam.setTenantId(tenantId);
        roleParam.setName("项目管理员");
        roleParam.setCreateBy(userId);
        Long id = add(roleParam);
        PushMessageDto pushMessageDto = new PushMessageDto();
        pushMessageDto.setRoleId(Long.toString(id));
        pushMessageDto.setTenantId(String.valueOf(roleParam.getTenantId()));
        pushMessageDto.setStatus("ADD");
        pushMessageDto.setAppKey(roleParam.getAppId());
        sendMessageToWebSocketService.sendMessage(pushMessageDto);
    }

    @Override
    public void authChange(Long authId, String appId, String type, String isNotTips, String userId) {
        PushMessageDto pushMessageDto = new PushMessageDto();
        pushMessageDto.setAppKey(appId);
        //为了兼容旧版暂时为IsInterface
        pushMessageDto.setIsInterface(isNotTips);
        if (type.equals(ADMIN)) {
            if (userId == null) {
                pushMessageDto.setRoleId(ADMIN);
                pushMessageDto.setTenantId("0");
                pushMessageDto.setStatus("ADD");
                sendMessageToWebSocketService.sendMessage(pushMessageDto);
            } else {
                PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
                pushAppOrderMsgDto.setAppKey(appId);
                pushAppOrderMsgDto.setTenantId("0");
                pushAppOrderMsgDto.setUserId(userId);
                pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
                sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
            }
            ;
        } else if (type.equals(ANONYMOUS)) {
            pushMessageDto.setRoleId(ANONYMOUS);
            pushMessageDto.setTenantId("-1");
            pushMessageDto.setStatus("ADD");
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
        } else {
            if (ObjectUtils.isNotEmpty(authId)) {
                List<RoleAuth> roleAuths = roleAuthMapper.selectList(Wrappers.<RoleAuth>lambdaQuery().eq(RoleAuth::getAuthId, authId).groupBy(RoleAuth::getRoleId));
                if (ObjectUtils.isNotEmpty(roleAuths)) {
                    for (RoleAuth roleAuth : roleAuths) {
                        TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, roleAuth.getRoleId()));
                        if (ObjectUtils.isNotEmpty(tenantRole)) {
                            pushMessageDto.setRoleId(String.valueOf(roleAuth.getRoleId()));
                            pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
                            pushMessageDto.setStatus("ADD");
                            sendMessageToWebSocketService.sendMessage(pushMessageDto);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void changeRoleStatus(RoleParam roleParam) {
        MdRole mdRole = new MdRole();
        mdRole.setId(roleParam.getId());
        mdRole.setStatus(roleParam.getStatus());
        roleMapper.updateById(mdRole);
        TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, roleParam.getId()));
        if (ObjectUtils.isNotEmpty(tenantRole)) {
            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setRoleId(String.valueOf(roleParam.getId()));
            pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
            if (roleParam.getStatus().equals(true)) {
                pushMessageDto.setStatus("ADD");
            } else {
                pushMessageDto.setStatus("DELETE");
            }
            pushMessageDto.setAppKey(tenantRole.getAppId());
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
        }
        MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
        mdOrgOperationLog.setOperationUserId(roleParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(roleParam));
        mdOrgOperationLog.setOperationUserName(roleParam.getOperateUserName());
        mdOrgOperationLog.setAppId(roleParam.getAppId());
        mdOrgOperationLog.setOperationType(RoleOperationType.STATUS_CHANGE.getCode());
        mdOrgOperationLog.setRoleId(roleParam.getId());
        mdOrgOperationLog.setUserIp(roleParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(roleParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        mdRoleOperationLogMapper.insert(mdOrgOperationLog);
    }

    @Override
    public void deleteAppRole(RoleParam roleParam) throws Exception {
        MdRoleOperationLog mdOrgOperationLog = new MdRoleOperationLog();
        mdOrgOperationLog.setOperationUserId(roleParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(roleParam));
        mdOrgOperationLog.setOperationUserName(roleParam.getOperateUserName());
        mdOrgOperationLog.setAppId(roleParam.getAppId());
        mdOrgOperationLog.setOperationType(RoleOperationType.DELETE.getCode());
        mdOrgOperationLog.setRoleId(roleParam.getId());
        mdOrgOperationLog.setUserIp(roleParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(roleParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        mdRoleOperationLogMapper.insert(mdOrgOperationLog);
        delete(roleParam.getId());
    }
}
