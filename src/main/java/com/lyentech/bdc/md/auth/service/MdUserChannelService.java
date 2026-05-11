package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
import com.lyentech.bdc.md.auth.model.param.AddUserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelGroupParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.vo.UserChannelRelatedVO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface MdUserChannelService {
    Map<String,Object> getChannelListByAppId(String appId, Long tenantId);

    Integer updateChannelById(UserChannelParam param);

    Integer addChannel(UserChannelParam param);

    Integer deleteChannel(UserChannelParam param);

    Integer addGroup(UserChannelGroupParam param);

    Integer updateGroup(UserChannelGroupParam param);

    Integer deleteGroup(UserChannelGroupParam param);

    void batchRelatedUser(UserChannelRelatedParam param);

    void removeRelatedUser(UserChannelRelatedParam param);

    PageResult<UserChannelRelatedVO> getChannelRelatedUserList(String appId, Long groupId, Long channelId, Long pageNum, Long pageSize, String name) throws UnsupportedEncodingException;

    List<Long> getChannelListByUser(String appId, Long tenantId, Long userId);

    List<UserChannel> getChannelList(String appId, Long tenantId, String name);

    List<UserChannel> getChannelListByUserId(String appId, Long tenantId,Long userId);

    void relatedUserChannel(AddUserChannelRelatedParam param) throws Exception;

    UserChannel getChannelByName(String appId, Long tenantId, String channel);

    List<UserChannel> getChannelListByUserChannel(String appId, Long tenantId, String name, Long userId);
}
