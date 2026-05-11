package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.md.auth.model.entity.Tenant;
import com.lyentech.bdc.md.auth.model.param.TenantParam;
import com.lyentech.bdc.md.auth.model.vo.TenantVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface TenantService extends IService<Tenant> {

    /**
     * 获取用户在指定app下的租户
     * @param appKey
     * @param userId
     * @return
     */
    List<TenantVO> getListByAppKeyAndUserId(String appKey, Long userId);

    TenantVO addTenant(TenantParam tenantParam);

    void  updateTenant(TenantParam tenantParam);

    TenantVO getOrgByTenant(Long tenantId);

    List<TenantVO> getList(String appKey, Long userId);

    String getName(Long tenantId);




}
