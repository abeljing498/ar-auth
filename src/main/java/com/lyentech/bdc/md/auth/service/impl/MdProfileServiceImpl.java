package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.dao.MdUserAppRoleMapper;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.entity.MdUserAppRole;
import com.lyentech.bdc.md.auth.service.*;
import com.lyentech.bdc.md.auth.util.PhoneNumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.ADMIN;
import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.DEFAULT_ROLE;

/**
 * @Author :yan
 * @Date :Create in 2022/8/9
 * @Description :
 */

@Service
public class MdProfileServiceImpl implements MdProfileService {
    @Value("${auth.login.page.url}")
    private String loginPageUrl;
    @Autowired
    private MdRoleService roleService;
    @Autowired
    private MdAppService appService;
    @Autowired
    private MdUserService mdUserService;
    @Resource
    private MdUserAppRoleMapper userAppRoleMapper;
    @Autowired
    private UserTenantRoleService userTenantRoleService;

    @Override
    public MdUser getMe(String appKey, MdUser user) {
        // 查询该 app 的主页地址
        String homepage = appService.getHomepage(appKey);
        Map<String, Object> additional = new HashMap<>(8);
        additional.put("homepage", homepage);
        additional.put("policyPage", loginPageUrl+"/policy/index.html?"+appKey);
        user.setAdditional(additional);
        if (user != null && user.getId() != null) {
            MdUser mdUser = mdUserService.getById(user.getId().intValue());
            if (mdUser != null) {
                user.setPhone(mdUser.getPhone());
            }
        }
        // 查询admin角色
        MdRole mdRole = roleService.getOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, ADMIN));
        Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getAppId, appKey)
                .eq(MdUserAppRole::getUserId, user.getId())
                .eq(MdUserAppRole::getRoleId, mdRole.getId()));
        if (integer == 0) {
            user.setSysAdmin(false);
        } else {
            user.setSysAdmin(true);
        }
        return user;
    }

    @Override
    public Set<String> getRoles(String appKey, Long tenantId, Long userId) {

        Set<String> roles = new HashSet<>();
        // 查询用户在该 app 下tenant中的角色
        roles = userTenantRoleService.getRoleNames(appKey, tenantId, userId);
        // 查询admin角色
        MdRole mdRole = roleService.getOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, ADMIN));
        Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getUserId, userId)
                .eq(MdUserAppRole::getRoleId, mdRole.getId())
                .eq(MdUserAppRole::getAppId, appKey));
        if (integer == 1) {
            roles.add(ADMIN);
        }

        if (roles == null || roles.isEmpty()) {
            roles.add(DEFAULT_ROLE);
        }
        return roles;
    }

    @Override
    public Map getRoleInfo(String appKey, Long tenantId, Long userId) {

        Set<String> roles = new HashSet<>();
        List<Long> roleIds = new ArrayList<>();
        // 查询用户在该 app 下tenant中的角色
        roles = (Set<String>) userTenantRoleService.getRoleInfoNames(appKey, tenantId, userId).get("roleNames");
        roleIds = (List<Long>) userTenantRoleService.getRoleInfoNames(appKey, tenantId, userId).get("roleIds");
        // 查询admin角色
        MdRole mdRole = roleService.getOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, ADMIN));
        Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getUserId, userId)
                .eq(MdUserAppRole::getRoleId, mdRole.getId())
                .eq(MdUserAppRole::getAppId, appKey));
        if (integer == 1) {
            roles.add(ADMIN);
        }

        if (roles == null || roles.isEmpty()) {
            roles.add(DEFAULT_ROLE);
        }
        Map roleMap = new HashMap();
        roleMap.put("roleNames", roles);
        roleMap.put("roleIds", roleIds);
        return roleMap;
    }
}
