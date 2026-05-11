package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.AppSso;
import com.lyentech.bdc.md.auth.model.entity.UserChannelRelated;
import com.lyentech.bdc.md.auth.model.vo.UserChannelRelatedVO;
import org.apache.ibatis.annotations.Param;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */

public interface UserChannelRelatedMapper extends BaseMapper<UserChannelRelated> {


    IPage<UserChannelRelatedVO> getChannelRelatedUserList(Page page, @Param("appId") String appId, @Param("groupId") Long groupId, @Param("channelId") Long channelId, @Param("name") String name);
}
