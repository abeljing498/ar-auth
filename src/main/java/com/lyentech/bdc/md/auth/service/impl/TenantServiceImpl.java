package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.TenantParam;
import com.lyentech.bdc.md.auth.model.vo.TenantVO;
import com.lyentech.bdc.md.auth.service.MdRoleService;
import com.lyentech.bdc.md.auth.service.TenantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.md.auth.util.RandomAccountGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {

    @Resource
    private AppTenantMapper appTenantMapper;
    @Resource
    private MdUserOrgMapper mdUserOrgMapper;
    @Resource
    private MdRoleMapper roleMapper;
    @Resource
    private MdUserAppRoleMapper userAppRoleMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private MdOrgExternalMapper mdOrgExternalMapper;
    @Resource
    private MdRoleService mdRoleService;
    @Resource
    private MdPermissionMapper mdPermissionMapper;
    @Value("${ark.search.app.id}")
    private String arkSearchAppId;
    @Value("${ark.manager.app.id}")
    private String arkManagerAppId;
    @Value("${faqc.app.id}")
    private String faqcAppId;
    @Value("${ar.manager.app.id}")
    private String arManagerAppId;

    @Override
    public List<TenantVO> getListByAppKeyAndUserId(String appKey, Long userId) {

        List<TenantVO> appTenants = appTenantMapper.getListByAppId(appKey);
        if (CollectionUtils.isEmpty(appTenants)) {
            appTenants = getDefaultTenants(appKey);
        }
        List<Long> tenantIds = appTenants.stream().map(TenantVO::getId).collect(Collectors.toList());
        // 判断是否为系统超管，是超管则返回所有租户
        MdRole mdRole = roleMapper.selectOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, MdAuthRoleConstant.ADMIN)
                .eq(MdRole::getType, 0));
        Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getUserId, userId)
                .eq(MdUserAppRole::getRoleId, mdRole.getId())
                .eq(MdUserAppRole::getAppId, appKey));
        if (integer == 1) {
            return appTenants;
        } else {
            List<MdUserOrg> mdUserOrgs = mdUserOrgMapper.selectList(Wrappers.<MdUserOrg>lambdaQuery()
                    .select(MdUserOrg::getTenantId)
                    .eq(MdUserOrg::getUserId, userId)
                    .in(MdUserOrg::getTenantId, tenantIds));
            List<Long> userTenantIds = mdUserOrgs.stream()
                    .map(MdUserOrg::getTenantId)
                    .collect(Collectors.toList());
            List<TenantVO> userTenants = appTenants.stream()
                    .filter(appTenant -> userTenantIds.contains(appTenant.getId()))
                    .collect(Collectors.toList());
            return userTenants;
        }
    }

    /**
     * 如果数据库中没有对应关系获取默认租户信息
     * @param appKey
     * @return
     */
    private List<TenantVO> getDefaultTenants(String appKey) {
        List<TenantVO> appTenants = new ArrayList<>();
        TenantVO tenantVO = new TenantVO();
        if (appKey.contains(arkSearchAppId)) {
            String []tenant = arkSearchAppId.split(",");
            tenantVO.setId(Long.valueOf(tenant[1]));
            tenantVO.setName("格力电器");
        } else if (appKey.contains(arkManagerAppId)) {
            String[] tenant= arkManagerAppId.split(",");
            tenantVO.setId(Long.valueOf(tenant[1]));
            tenantVO.setName("格力电器");

        } else if (appKey.contains(faqcAppId)) {
            String[] tenant = faqcAppId.split(",");
            tenantVO.setId(Long.valueOf(tenant[1]));
            tenantVO.setName("格力智能知识问答平台");

        } else if (appKey.contains(arManagerAppId)) {
            String[] tenant = arManagerAppId.split(",");
            tenantVO.setId(Long.valueOf(tenant[1]));
            tenantVO.setName("权限管理平台");

        } else {
            String[] tenant = arkManagerAppId.split(",");
            tenantVO.setId(Long.valueOf(tenant[1]));
            tenantVO.setName("格力电器");
        }
        appTenants.add(tenantVO);
        return appTenants;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantVO addTenant(TenantParam tenantParam) {
        if (StringUtils.isBlank(tenantParam.getAppId())) {
            throw new IllegalParamException("appId不能为空");
        }
        if (StringUtils.isBlank(tenantParam.getName())) {
            throw new IllegalParamException("租户名称不能为空");
        }
        Integer count = tenantMapper.selectCount(Wrappers.<Tenant>lambdaQuery()
                .eq(Tenant::getName, tenantParam.getName()));
        if (count > 0) {
            throw new IllegalParamException("该租户已存在，请确认企业名称后重新提交！");
        }
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(tenantParam, tenant);
        tenant.setStatus(1);
        tenantMapper.insert(tenant);
        //插入项目与租户的关联关系
        AppTenant appTenant = new AppTenant();
        appTenant.setAuthApp(1);
        appTenant.setAppId(tenantParam.getAppId());
        appTenant.setTenantId(tenant.getId());
        appTenantMapper.insert(appTenant);
        //插入顶层组织
        Org org = new Org();
        org.setTenantId(tenant.getId());
        org.setName(tenant.getName());
        org.setPid(0L);
        org.setHeight(0L);
        orgMapper.insert(org);
        //插入自定义ID
        MdOrgExternal mdOrgExternal = new MdOrgExternal();
        mdOrgExternal.setTenantId(tenant.getId());
        mdOrgExternal.setOrgId(org.getId());
        String randomAccount = RandomAccountGenerator.getRandomAccount(5);
        mdOrgExternal.setCustomId(randomAccount);
        mdOrgExternalMapper.insert(mdOrgExternal);
        TenantVO tenantVO = new TenantVO();
        tenantVO.setId(tenant.getId());
        tenantVO.setName(tenant.getName());
        tenantVO.setOrgId(org.getId());
        // 添加一个管理员角色，并赋予他全部权限
        if (tenantParam.getAutoRole().equals(true)) {
            mdRoleService.addAdminRole(tenant.getId(), tenantParam.getAppId(), tenantParam.getUserId());
        }
        return tenantVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenant(TenantParam tenantParam) {
        if (ObjectUtils.isEmpty(tenantParam.getId())) {
            throw new IllegalParamException("租户id不能为空");
        }
        Tenant tenant = baseMapper.selectById(tenantParam.getId());
        Org org = orgMapper.selectOne(Wrappers.<Org>lambdaQuery()
                .eq(Org::getTenantId, tenantParam.getId())
                .eq(Org::getPid, 0));
        //要判断是否重复
        Integer count = tenantMapper.selectCount(Wrappers.<Tenant>lambdaQuery()
                .eq(Tenant::getName, tenantParam.getName())
                .ne(Tenant::getId, tenantParam.getId()));
        if (count > 0) {
            throw new IllegalParamException("该租户已存在，请确认企业名称后重新提交！");
        }
        //租户名更改，组织名更改
        org.setName(tenantParam.getName());
        orgMapper.updateById(org);
        tenant.setName(tenantParam.getName());
        tenant.setDescription(tenantParam.getDescription());
        tenantMapper.updateById(tenant);
    }

    @Override
    public TenantVO getOrgByTenant(Long tenantId) {
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户id不能为空");
        }
        TenantVO tenantVO = new TenantVO();
        Org org = orgMapper.selectOne(Wrappers.<Org>lambdaQuery()
                .eq(Org::getTenantId, tenantId)
                .eq(Org::getPid, 0));
        if (ObjectUtils.isNotEmpty(org)) {
            tenantVO.setOrgId(org.getId());
        }
        tenantVO.setId(tenantId);
        return tenantVO;
    }

    @Override
    public List<TenantVO> getList(String appKey, Long userId) {
        List<TenantVO> appTenants = appTenantMapper.getListByAppId(appKey);
        List<Long> tenantIds = appTenants.stream().map(TenantVO::getId).collect(Collectors.toList());
        // 判断是否为系统超管，是超管则返回所有租户
        MdRole mdRole = roleMapper.selectOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, MdAuthRoleConstant.ADMIN)
                .eq(MdRole::getType, 0)
                .eq(MdRole::getStatus, true));
        Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getUserId, userId)
                .eq(MdUserAppRole::getRoleId, mdRole.getId())
                .eq(MdUserAppRole::getAppId, appKey));
        if (integer == 1) {
            for (TenantVO tenantVO : appTenants) {
                tenantVO.setAuth(true);
            }
            return appTenants;
        } else {
            List<MdUserOrg> mdUserOrgs = mdUserOrgMapper.selectList(Wrappers.<MdUserOrg>lambdaQuery()
                    .select(MdUserOrg::getTenantId)
                    .eq(MdUserOrg::getUserId, userId)
                    .in(MdUserOrg::getTenantId, tenantIds));
            List<Long> userTenantIds = mdUserOrgs.stream()
                    .map(MdUserOrg::getTenantId)
                    .collect(Collectors.toList());
            Set<Long> byPermission = mdPermissionMapper.getByPermission(appKey, "/project/exportDailyGoal", userId);
            List<TenantVO> userTenants = appTenants.stream()
                    .filter(appTenant -> userTenantIds.contains(appTenant.getId()))
                    .collect(Collectors.toList());
            for (TenantVO tenantVO : userTenants) {
                boolean contains = byPermission.contains(tenantVO.getId());
                tenantVO.setAuth(contains);
            }
            return userTenants;
        }
    }

    @Override
    public String getName(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        TenantVO tenantVO = new TenantVO();
        if (ObjectUtils.isNotEmpty(tenant)) {
            BeanUtils.copyProperties(tenant, tenantVO);
            return tenantVO.getName();
        } else {
            return null;
        }
    }
}
