package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.Auth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.param.GroupAuthsParam;
import com.lyentech.bdc.md.auth.model.vo.AuthVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface AuthMapper extends BaseMapper<Auth> {

    /**
     * 根据roleId获取authId列表
     *
     * @param roleId
     * @return
     */
    List<Long> getAuthId(@Param("roleId") Long roleId);

    List<AuthVO> getGroupAuth(@Param("appId") String appId);

    List<AuthVO> getAuthTree(@Param("roleId") Long roleId);

    Integer getAuthNum(@Param("groupId") Long groupId);
}
