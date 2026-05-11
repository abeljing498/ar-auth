package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.MdUserStatusConstant;
import com.lyentech.bdc.md.auth.common.constant.UserOperationType;
import com.lyentech.bdc.md.auth.dao.MdBlackLogMapper;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserOrgMapper;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.BlackUserListParam;
import com.lyentech.bdc.md.auth.model.param.BlackUserParam;
import com.lyentech.bdc.md.auth.model.param.JoinBlackListParam;
import com.lyentech.bdc.md.auth.model.param.JoinBlackUserParam;
import com.lyentech.bdc.md.auth.model.vo.LoginLogVO;
import com.lyentech.bdc.md.auth.model.vo.MdBlackUserVO;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.service.MdBlackUserService;
import com.lyentech.bdc.md.auth.service.MdUserOperationLogService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 10:50
 */
@Service
@Slf4j
public class MdBlackUserServiceImpl extends ServiceImpl<MdBlackUserMapper, MdBlackUser> implements MdBlackUserService {

    @Autowired
    MdBlackUserMapper mdBlackUserMapper;
    @Autowired
    MdBlackLogMapper mdBlackLogMapper;
    @Autowired
    MdUserOrgMapper mdUserOrgMapper;
    @Resource
    MdUserMapper mdUserMapper;
    @Resource
    MdUserOperationLogService mdUserOperationLogService;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinBlack(BlackUserParam blackUserParam) {
        if (ObjectUtils.isEmpty(blackUserParam.getUserId())) {
            throw new IllegalParamException("用户Id不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getReason())) {
            throw new IllegalParamException("加入原因不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getOid())) {
            throw new IllegalParamException("操作人id不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getAppId())) {
            throw new IllegalParamException("项目id不能为空");
        }
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(blackUserParam.getOperateUserId());
        mdUserOperationLog.setOperateUserName(blackUserParam.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.BLACK.getCode());
        mdUserOperationLog.setAppId(blackUserParam.getAppId());
        mdUserOperationLog.setTenantId(blackUserParam.getTenantId());
        mdUserOperationLog.setUserId(blackUserParam.getUserId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(blackUserParam.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        MdBlackUser mdBlackUser = new MdBlackUser();
        BeanUtils.copyProperties(blackUserParam, mdBlackUser);
        MdBlackUser blackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery()
                .eq(MdBlackUser::getAppId, blackUserParam.getAppId())
                .eq(MdBlackUser::getUserId, blackUserParam.getUserId()));
        if (ObjectUtils.isNotEmpty(blackUser)) {
            throw new IllegalParamException("此用户已存在于本系统黑名单，请勿重复添加");
        }
        MdUser mdUser = mdUserMapper.selectById(blackUserParam.getOid());
        mdBlackUser.setOperName(mdUser.getNickname());
        mdBlackUserMapper.insert(mdBlackUser);
        //插入黑名单日志
        MdBlackLog mdBlackLog = new MdBlackLog();
        BeanUtils.copyProperties(blackUserParam, mdBlackLog);
        mdBlackLog.setType("JOIN");
        mdBlackLog.setBid(mdBlackUser.getId());
        mdBlackLog.setOperName(mdUser.getNickname());
        mdBlackLogMapper.insert(mdBlackLog);
        List<MdUserOrg> mdUserOrgList = mdUserOrgMapper.selectList(new QueryWrapper<MdUserOrg>().eq("tenant_id", blackUserParam.getTenantId()).eq("user_id", blackUserParam.getUserId()));
        for (MdUserOrg mdUserOrg : mdUserOrgList) {
            mdUserOrg.setStatus(MdUserStatusConstant.BLACK);
            mdUserOrgMapper.update(mdUserOrg, new QueryWrapper<MdUserOrg>().eq("user_id"
                    , mdUserOrg.getUserId()).eq("tenant_id", mdUserOrg.getTenantId()).eq("org_id", mdUserOrg.getOrgId()));
        }
        //加入黑名单推送消息至下游
        PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
        pushAppOrderMsgDto.setAppKey(blackUserParam.getAppId());
        pushAppOrderMsgDto.setUserId(blackUserParam.getUserId().toString());
        pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.IS_BLACK);
        sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public void listJoinUser(JoinBlackListParam blackListParam) {
        log.info("用户加入黑名单{}", JSON.toJSONString(blackListParam));
        if (ObjectUtils.isEmpty(blackListParam.getOid())) {
            throw new IllegalParamException("操作人id不能为空");
        }
        if (ObjectUtils.isEmpty(blackListParam.getAppId())) {
            throw new IllegalParamException("项目id不能为空");
        }
        if (ObjectUtils.isEmpty(blackListParam.getJoinUserList())) {
            throw new IllegalParamException("人员不能为空");
        }

        MdUser mdUser = mdUserMapper.selectById(blackListParam.getOid());
        for (JoinBlackUserParam blackUserParam : blackListParam.getJoinUserList()) {
            if (ObjectUtils.isEmpty(blackUserParam.getUserId())) {
                throw new IllegalParamException("用户Id不能为空");
            }
            if (ObjectUtils.isEmpty(blackUserParam.getReason())) {
                throw new IllegalParamException("加入原因不能为空");
            }
            MdBlackUser blackUser = mdBlackUserMapper.selectOne(
                    Wrappers.<MdBlackUser>lambdaQuery()
                            .eq(MdBlackUser::getAppId, blackListParam.getAppId())
                            .eq(MdBlackUser::getUserId, blackUserParam.getUserId()));

            // 不存在才插入黑名单
            if (ObjectUtils.isEmpty(blackUser)) {
                MdBlackUser mdBlackUser = new MdBlackUser();
                BeanUtils.copyProperties(blackUserParam, mdBlackUser);
                mdBlackUser.setOperName(mdUser.getNickname());
                mdBlackUser.setAppId(blackListParam.getAppId());
                mdBlackUserMapper.insert(mdBlackUser);

                MdBlackLog mdBlackLog = new MdBlackLog();
                BeanUtils.copyProperties(blackUserParam, mdBlackLog);
                mdBlackLog.setType("JOIN");
                mdBlackLog.setAppId(blackListParam.getAppId());
                mdBlackLog.setBid(mdBlackUser.getId());
                mdBlackLog.setOperName(mdUser.getNickname());
                mdBlackLog.setOid(blackListParam.getOid());
                mdBlackLogMapper.insert(mdBlackLog);

                MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
                mdUserOperationLog.setOperateUserId(blackUserParam.getOperateUserId());
                mdUserOperationLog.setOperateUserName(blackUserParam.getOperateUserName());
                mdUserOperationLog.setOperationType(UserOperationType.BLACK.getCode());
                mdUserOperationLog.setAppId(blackUserParam.getAppId());
                mdUserOperationLog.setTenantId(blackUserParam.getTenantId());
                mdUserOperationLog.setUserId(blackUserParam.getUserId());
                mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
                mdUserOperationLog.setCreateTime(new Date());
                mdUserOperationLog.setUserIp(blackUserParam.getUserIp());
                mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            }

            // 不管黑名单记录是否已存在，都更新状态
            mdUserOrgMapper.update(
                    null,
                    Wrappers.<MdUserOrg>lambdaUpdate()
                            .eq(MdUserOrg::getTenantId, blackUserParam.getTenantId())
                            .eq(MdUserOrg::getUserId, blackUserParam.getUserId())
                            .set(MdUserOrg::getStatus, MdUserStatusConstant.BLACK)
            );

            PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
            pushAppOrderMsgDto.setAppKey(blackListParam.getAppId());
            pushAppOrderMsgDto.setUserId(blackUserParam.getUserId().toString());
            pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.IS_BLACK);
            sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBlack(BlackUserParam blackUserParam) {
        if (ObjectUtils.isEmpty(blackUserParam.getUserId())) {
            throw new IllegalParamException("用户Id不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getReason())) {
            throw new IllegalParamException("移除原因不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getOid())) {
            throw new IllegalParamException("操作人id不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getAppId())) {
            throw new IllegalParamException("项目id不能为空");
        }
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(blackUserParam.getOperateUserId());
        mdUserOperationLog.setOperateUserName(blackUserParam.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.UNLACK.getCode());
        mdUserOperationLog.setAppId(blackUserParam.getAppId());
        mdUserOperationLog.setTenantId(blackUserParam.getTenantId());
        mdUserOperationLog.setUserId(blackUserParam.getUserId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(blackUserParam.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        MdBlackUser mdBlackUser = new MdBlackUser();
        BeanUtils.copyProperties(blackUserParam, mdBlackUser);
        mdBlackUserMapper.delete(Wrappers.<MdBlackUser>lambdaQuery()
                .eq(MdBlackUser::getUserId, blackUserParam.getUserId())
                .eq(MdBlackUser::getAppId, blackUserParam.getAppId()));
        //插入黑名单日志
        MdBlackLog mdBlackLog = new MdBlackLog();
        BeanUtils.copyProperties(blackUserParam, mdBlackLog);
        mdBlackLog.setType("REMOVE");
        mdBlackLog.setBid(mdBlackUser.getId());
        MdUser mdUser = mdUserMapper.selectById(blackUserParam.getOid());
        if (ObjectUtils.isEmpty(mdUser)) {
            mdBlackLog.setOperName(null);
        }
        mdBlackLog.setOperName(mdUser.getNickname());
        mdBlackLogMapper.insert(mdBlackLog);
        List<MdUserOrg> mdUserOrgList = mdUserOrgMapper.selectList(new QueryWrapper<MdUserOrg>().eq("tenant_id", blackUserParam.getTenantId()).eq("user_id", blackUserParam.getUserId()));
        for (MdUserOrg mdUserOrg : mdUserOrgList) {
            mdUserOrg.setStatus(MdUserStatusConstant.NORMAL);
            mdUserOrgMapper.update(mdUserOrg, new QueryWrapper<MdUserOrg>().eq("user_id"
                    , mdUserOrg.getUserId()).eq("tenant_id", mdUserOrg.getTenantId()).eq("org_id", mdUserOrg.getOrgId()));
        }
        PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
        pushAppOrderMsgDto.setAppKey(blackUserParam.getAppId());
        pushAppOrderMsgDto.setUserId(blackUserParam.getUserId().toString());
        pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.REMOVE_BLACK);
        sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBlack(BlackUserParam blackUserParam) {
        if (ObjectUtils.isEmpty(blackUserParam.getId())) {
            throw new IllegalParamException("黑名单Id不能为空");
        }
        if (ObjectUtils.isEmpty(blackUserParam.getReason())) {
            throw new IllegalParamException("移除原因不能为空");
        }
        MdBlackUser mdBlackUser = new MdBlackUser();
        MdBlackUser blackUser = mdBlackUserMapper.selectById(blackUserParam.getId());
        BeanUtils.copyProperties(blackUser, mdBlackUser);
        mdBlackUser.setReason(blackUserParam.getReason());
        mdBlackUserMapper.updateById(mdBlackUser);
        //插入黑名单日志
        MdBlackLog mdBlackLog = new MdBlackLog();
        BeanUtils.copyProperties(blackUser, mdBlackLog);
        mdBlackLog.setType("UPDATE");
        mdBlackLog.setReason(blackUserParam.getReason());
        mdBlackLog.setBid(mdBlackUser.getId());
        MdUser mdUser = mdUserMapper.selectById(blackUserParam.getOid());
        mdBlackLog.setOperName(mdUser.getNickname());
        mdBlackLog.setOid(blackUserParam.getOid());
        mdBlackLog.setCreateTime(new Date());
        mdBlackLogMapper.insert(mdBlackLog);
    }

    @Override
    public List<Long> listExistUser(String appId) {
        List<MdBlackUser> mdBlackUsers = mdBlackUserMapper.selectList(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, appId));
        List<Long> userIds = mdBlackUsers.stream()
                .map(MdBlackUser::getUserId)
                .collect(Collectors.toList());
        return userIds;
    }

    @Override
    public Boolean getIsBlack(String clientId, Long userId) {
        MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getUserId, userId).eq(MdBlackUser::getAppId, clientId));
        if (ObjectUtils.isEmpty(mdBlackUser)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public PageResult<MdBlackUserVO> getUserList(Long pageNum, Long pageSize, String appId, String keyword, String beginTime, String endTime, String reason) {
        if (ObjectUtils.isEmpty(appId)) {
            throw new IllegalParamException("项目id不能为空");
        }
        Page<MdBlackUserVO> page = new Page(pageNum, pageSize);
        BlackUserListParam blackUserListParam = new BlackUserListParam();
        blackUserListParam.setPageNum(pageNum);
        blackUserListParam.setPageSize(pageSize);
        blackUserListParam.setAppId(appId);
        blackUserListParam.setKeyword(keyword);
        blackUserListParam.setBeginTime(beginTime);
        blackUserListParam.setEndTime(endTime);
        blackUserListParam.setReason(reason);
        IPage<MdBlackUserVO> blackUserPage = mdBlackUserMapper.getUserList(page, blackUserListParam);
        if (ObjectUtils.isNotEmpty(blackUserPage)) {
            for (MdBlackUserVO mdBlackUserVO : blackUserPage.getRecords()) {
                Integer count = mdBlackLogMapper.selectCount(Wrappers.<MdBlackLog>lambdaQuery()
                        .eq(MdBlackLog::getUserId, mdBlackUserVO.getUserId())
                        .eq(MdBlackLog::getAppId, appId)
                        .eq(MdBlackLog::getType, "JOIN"));
                mdBlackUserVO.setBlackNum(count);
            }
        }
        return PageResult.build(pageNum, pageSize, blackUserPage.getPages(), blackUserPage.getTotal(), blackUserPage.getRecords());
    }


    @Override
    public MdBlackUserVO searchUser(String keyword, String appId) {
        MdUser mdUser = mdUserMapper.searchByAccount(keyword);
        if (ObjectUtils.isEmpty(mdUser)) {
            mdUser = mdUserMapper.searchByEmail(keyword);
            if (ObjectUtils.isEmpty(mdUser)) {
                mdUser = mdUserMapper.searchByPhone(keyword);
                if (ObjectUtils.isEmpty(mdUser)) {
                    throw new IllegalParamException("找不到该用户");
                }
            }
        }
        MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery()
                .eq(MdBlackUser::getAppId, appId).eq(MdBlackUser::getUserId, mdUser.getId()));
        if (ObjectUtils.isEmpty(mdBlackUser)) {
            return null;
        } else {
            MdBlackUserVO mdBlackUserVO = new MdBlackUserVO();
            mdBlackUserVO.setId(mdBlackUser.getId());
            mdBlackUserVO.setUserId(mdBlackUser.getUserId());
            mdBlackUserVO.setEmail(mdUser.getEmail());
            mdBlackUserVO.setPhone(mdUser.getPhone());
            mdBlackUserVO.setAccount(mdUser.getAccount());
            mdBlackUserVO.setReason(mdBlackUser.getReason());
            mdBlackUserVO.setCreateTime(mdBlackUser.getCreateTime());
            return mdBlackUserVO;
        }
    }

}
