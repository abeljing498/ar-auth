package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.AppTenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.vo.TenantVO;
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
public interface AppTenantMapper extends BaseMapper<AppTenant> {

    /**
     * 获取启用状态的租户
     *
     * @param appId
     * @return
     */
    List<TenantVO> getListByAppId(@Param("appId") String appId);

    List<TenantVO> getTenantByAppId(@Param("appId") String appId);

}
