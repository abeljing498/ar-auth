package com.lyentech.bdc.md.auth.service.impl;

import com.lyentech.bdc.md.auth.model.entity.AppTenant;
import com.lyentech.bdc.md.auth.dao.AppTenantMapper;
import com.lyentech.bdc.md.auth.service.AppTenantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Service
public class AppTenantServiceImpl extends ServiceImpl<AppTenantMapper, AppTenant> implements AppTenantService {

}
