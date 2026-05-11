package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.AppSso;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author :yan
 * @Date :Create in 2022/7/4
 * @Description :
 */

public interface UserChannelMapper extends BaseMapper<UserChannel> {

    List<UserChannel> getChannelListByUserChannel(@Param("appId")String appId, @Param("tenantId")Long tenantId, @Param("name")String name, @Param("userId")Long userId);
}
