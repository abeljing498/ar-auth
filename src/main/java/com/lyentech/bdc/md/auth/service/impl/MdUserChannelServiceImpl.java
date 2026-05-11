package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.dao.UserChannelGroupMapper;
import com.lyentech.bdc.md.auth.dao.UserChannelMapper;
import com.lyentech.bdc.md.auth.dao.UserChannelRelatedMapper;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
import com.lyentech.bdc.md.auth.model.entity.UserChannelGroup;
import com.lyentech.bdc.md.auth.model.entity.UserChannelRelated;
import com.lyentech.bdc.md.auth.model.param.AddUserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelGroupParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.vo.UserChannelRelatedVO;
import com.lyentech.bdc.md.auth.model.vo.UserChannelVO;
import com.lyentech.bdc.md.auth.service.MdUserChannelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author 260442
 */
@Service
public class MdUserChannelServiceImpl implements MdUserChannelService {
    @Autowired
    private UserChannelGroupMapper userChannelGroupMapper;
    @Autowired
    private UserChannelMapper userChannelMapper;
    @Autowired
    private UserChannelRelatedMapper userChannelRelatedMapper;

    /**
     * 查询
     *
     * @param appId
     * @param tenantId
     * @return
     */
    @Override
    public Map<String, Object> getChannelListByAppId(String appId, Long tenantId) {
        Map<String, Object> map = new HashMap();
        List<UserChannelGroup> userChannelGroups = userChannelGroupMapper.selectList(new QueryWrapper<UserChannelGroup>().eq("app_id", appId).eq("tenant_id", tenantId));
        List<UserChannelVO> userChannelVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userChannelGroups)) {
            for (UserChannelGroup userChannelGroup :
                    userChannelGroups) {
                UserChannelVO userChannelVO = new UserChannelVO();
                BeanUtils.copyProperties(userChannelGroup, userChannelVO);
                List<UserChannel> userChannelList = userChannelMapper.selectList(new QueryWrapper<UserChannel>().eq("group_id", userChannelGroup.getId()).eq("app_id", userChannelGroup.getAppId()));
                userChannelVO.setUserChannelList(userChannelList);
                userChannelVOS.add(userChannelVO);
            }
        }
        map.put("channelGroups", userChannelVOS);
        return map;
    }

    @Override
    public Integer updateChannelById(UserChannelParam param) {
        if (param.getId() == null) {
            throw new IllegalParamException("Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getName())) {
            throw new IllegalParamException("渠道名称不能为空！");
        }
        if (StringUtils.isEmpty(param.getGroupId())) {
            throw new IllegalParamException("渠道组不能为空！");
        }
        UserChannel userChannel = userChannelMapper.selectOne(new QueryWrapper<UserChannel>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("name", param.getName()));
        if (userChannel != null) {
            throw new IllegalParamException("用户群体名称已重复！");
        }
        userChannel = new UserChannel();
        BeanUtils.copyProperties(param, userChannel);
        return userChannelMapper.updateById(userChannel);


    }

    @Override
    public Integer addChannel(UserChannelParam param) {
        if (StringUtils.isEmpty(param.getName())) {
            throw new IllegalParamException("渠道名称不能为空！");
        }
        if (StringUtils.isEmpty(param.getGroupId())) {
            throw new IllegalParamException("渠道组不能为空！");
        }
        if (StringUtils.isEmpty(param.getTenantId())) {
            throw new IllegalParamException("组户Id不能为空！");
        }
        UserChannel userChannel = userChannelMapper.selectOne(new QueryWrapper<UserChannel>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("name", param.getName()));
        if (userChannel != null) {
            throw new IllegalParamException("用户群体名称已重复！");
        }
        userChannel = new UserChannel();
        BeanUtils.copyProperties(param, userChannel);
        return userChannelMapper.insert(userChannel);
    }

    @Override
    public Integer deleteChannel(UserChannelParam param) {
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getGroupId())) {
            throw new IllegalParamException("渠道组不能为空！");
        }
        if (StringUtils.isEmpty(param.getId())) {
            throw new IllegalParamException("Id不能为空！");
        }
        Integer count = userChannelRelatedMapper.selectCount(new QueryWrapper<UserChannelRelated>().eq("app_id", param.getAppId()).eq("channel_id", param.getId()));
        if (count > 0) {
            throw new IllegalParamException("该用户群体中存在成员，暂不可删除！");
        }
        return userChannelMapper.delete(new QueryWrapper<UserChannel>().eq("app_id", param.getAppId()).eq("group_id", param.getGroupId()).eq("id", param.getId()));
    }

    @Override
    public Integer addGroup(UserChannelGroupParam param) {
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getTenantId())) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getName())) {
            throw new IllegalParamException("分组名称不能为空！");
        }
        UserChannelGroup userChannelGroup = userChannelGroupMapper.selectOne(new QueryWrapper<UserChannelGroup>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("name", param.getName()));
        if (userChannelGroup != null) {
            throw new IllegalParamException("分组名称已重复！");
        }
        userChannelGroup = new UserChannelGroup();
        BeanUtils.copyProperties(param, userChannelGroup);

        return userChannelGroupMapper.insert(userChannelGroup);
    }

    @Override
    public Integer updateGroup(UserChannelGroupParam param) {
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getTenantId())) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getName())) {
            throw new IllegalParamException("分组名称不能为空！");
        }
        if (StringUtils.isEmpty(param.getId())) {
            throw new IllegalParamException("Id不能为空！");
        }
        UserChannelGroup userChannelGroup = userChannelGroupMapper.selectOne(new QueryWrapper<UserChannelGroup>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("name", param.getName()));
        if (userChannelGroup != null) {
            throw new IllegalParamException("分组名称已重复！");
        }
        userChannelGroup = new UserChannelGroup();
        BeanUtils.copyProperties(param, userChannelGroup);
        return userChannelGroupMapper.updateById(userChannelGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteGroup(UserChannelGroupParam param) {
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getId())) {
            throw new IllegalParamException("Id不能为空！");
        }
        Integer count = userChannelMapper.selectCount(new QueryWrapper<UserChannel>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("group_id", param.getId()));
        if (count > 0) {
            throw new IllegalParamException("该分组下已存在群体，请删除群体后进行操作！");
        }
        Integer flag = userChannelGroupMapper.delete(new QueryWrapper<UserChannelGroup>().eq("id", param.getId()));
        userChannelMapper.delete(new QueryWrapper<UserChannel>().eq("app_id", param.getAppId()).eq("tenant_id", param.getTenantId()).eq("group_id", param.getId()));
        return flag;

    }

    @Override
    public void batchRelatedUser(UserChannelRelatedParam param) {

        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
//        if (StringUtils.isEmpty(param.getGroupId())) {
//            throw new IllegalParamException("分组Id不能为空！");
//        }
        if (StringUtils.isEmpty(param.getChannelId())) {
            throw new IllegalParamException("渠道Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getTenantId())) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if (CollectionUtils.isEmpty(param.getList())) {
            throw new IllegalParamException("请选择用户！");
        }
        for (UserChannelRelated userChannelParam : param.getList()) {
            UserChannelRelated userChannelRelated = userChannelRelatedMapper.selectOne(new QueryWrapper<UserChannelRelated>()
                    .eq("app_id", param.getAppId())
//                    .eq("group_id", param.getGroupId())
                    .eq("channel_id", param.getChannelId())
                    .eq("user_id", userChannelParam.getUserId())
                    .eq("tenant_id", param.getTenantId())
                    .last("limit 1"));
            if (userChannelRelated == null) {
                userChannelRelated = new UserChannelRelated();
                userChannelRelated.setAppId(param.getAppId());
                userChannelRelated.setChannelId(param.getChannelId());
                userChannelRelated.setGroupId(param.getGroupId());
                userChannelRelated.setUserId(userChannelParam.getUserId());
                userChannelRelated.setTenantId(param.getTenantId());
                userChannelRelatedMapper.insert(userChannelRelated);
            }

        }

    }

    @Override
    public void removeRelatedUser(UserChannelRelatedParam param) {

        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
//        if (StringUtils.isEmpty(param.getGroupId())) {
//            throw new IllegalParamException("分组Id不能为空！");
//        }
        if (StringUtils.isEmpty(param.getChannelId())) {
            throw new IllegalParamException("渠道Id不能为空！");
        }
        if (CollectionUtils.isEmpty(param.getList())) {
            throw new IllegalParamException("请选择用户！");
        }
        for (UserChannelRelated userChannelParam : param.getList()) {
            userChannelRelatedMapper.delete(new QueryWrapper<UserChannelRelated>().eq("app_id", param.getAppId())
                    .eq("channel_id", param.getChannelId())
                    .eq("user_id", userChannelParam.getUserId()));

        }
    }

    @Override
    public PageResult<UserChannelRelatedVO> getChannelRelatedUserList(String appId, Long groupId, Long channelId, Long pageNum, Long pageSize, String name) throws UnsupportedEncodingException {
        name = !name.isEmpty() ? URLDecoder.decode(name, "UTF-8") : name;
        Page page = new Page(pageNum, pageSize);
        IPage<UserChannelRelatedVO> result = userChannelRelatedMapper.getChannelRelatedUserList(page, appId, groupId, channelId, name);
        return PageResult.build(pageNum, pageSize, result.getPages(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Long> getChannelListByUser(String appId, Long tenantId, Long userId) {
        List<UserChannelRelated> userChannelRelatedList = userChannelRelatedMapper.selectList(new QueryWrapper<UserChannelRelated>().eq("app_id", appId)
                .eq("tenant_id", tenantId)
                .eq("user_id", userId).groupBy("channel_id"));
        List<Long> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(userChannelRelatedList)) {
            return null;
        }
        for (UserChannelRelated userChannelRelated :
                userChannelRelatedList) {
            list.add(userChannelRelated.getChannelId());
        }
        return list;
    }

    @Override
    public List<UserChannel> getChannelList(String appId, Long tenantId, String name) {
        if (StringUtils.isEmpty(appId)) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        List<UserChannel> userChannelList = null;
        if (StringUtils.isEmpty(name)) {
            userChannelList = userChannelMapper.selectList(new QueryWrapper<UserChannel>().eq("app_id", appId).eq("tenant_id", tenantId));
        } else {
            if(!StringUtils.isEmpty(name)){
                try {
                    name= URLDecoder.decode(name, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            userChannelList = userChannelMapper.selectList(new QueryWrapper<UserChannel>().eq("app_id", appId).eq("tenant_id", tenantId).like("name","%"+name+"%"));
            if(!CollectionUtils.isEmpty(userChannelList)){
                for (UserChannel userChannel:userChannelList){
                    UserChannelGroup userChannelGroup= userChannelGroupMapper.selectOne(new QueryWrapper<UserChannelGroup>().eq("id",userChannel.getGroupId()).last("limit 1"));
                    userChannel.setGroupName(userChannelGroup.getName());
                }

            }

        }
        return userChannelList;

    }

    @Override
    public List<UserChannel> getChannelListByUserId(String appId, Long tenantId, Long userId) {
        List<UserChannelRelated> userChannelRelatedList = userChannelRelatedMapper.selectList(new QueryWrapper<UserChannelRelated>().eq("app_id", appId)
                .eq("tenant_id", tenantId)
                .eq("user_id", userId).groupBy("channel_id"));
        List<UserChannel> userChannelList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userChannelRelatedList)) {
            for (UserChannelRelated userChannelRelated : userChannelRelatedList) {
                UserChannel userChannel = userChannelMapper.selectOne(new QueryWrapper<UserChannel>().eq("id", userChannelRelated.getChannelId()));
                if (userChannel != null) {
                    userChannelList.add(userChannel);
                }
            }
        }
        return userChannelList;
    }

    @Override
    public void relatedUserChannel(AddUserChannelRelatedParam param) {
        if (StringUtils.isEmpty(param.getAppId())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getTenantId())) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if (StringUtils.isEmpty(param.getUserId())) {
            throw new IllegalParamException("用户Id不能为空！");
        }
        userChannelRelatedMapper.delete(new QueryWrapper<UserChannelRelated>()
                .eq("app_id", param.getAppId())
                .eq("user_id", param.getUserId())
                .eq("tenant_id", param.getTenantId()));
        if (!CollectionUtils.isEmpty(param.getChannelIds())) {
            for (Long channelId : param.getChannelIds()) {
                UserChannelRelated userChannelRelated = new UserChannelRelated();
                userChannelRelated.setAppId(param.getAppId());
                userChannelRelated.setChannelId(channelId);
                userChannelRelated.setUserId(param.getUserId());
                userChannelRelated.setTenantId(param.getTenantId());
                userChannelRelatedMapper.insert(userChannelRelated);
            }
        }

    }

    @Override
    public UserChannel getChannelByName(String appId, Long tenantId, String channel) {
        UserChannel userChannel = userChannelMapper.selectOne(new QueryWrapper<UserChannel>()
                .eq("app_id", appId)
                .eq("tenant_id", tenantId)
                .eq("name", channel)
                .last("limit 1"));
        return userChannel;
    }

    @Override
    public List<UserChannel> getChannelListByUserChannel(String appId, Long tenantId, String name, Long userId) {
        if (StringUtils.isEmpty(appId)) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (ObjectUtils.isEmpty(userId)) {
            throw new IllegalParamException("用户Id不能为空！");
        }
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if(!StringUtils.isEmpty(name)){
            try {
                name= URLDecoder.decode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return userChannelMapper.getChannelListByUserChannel( appId,tenantId,name,userId);
    }
}
