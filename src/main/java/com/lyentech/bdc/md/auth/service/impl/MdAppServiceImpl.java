package com.lyentech.bdc.md.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.constant.MdGrantTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.ResultCodeExtend;
import com.lyentech.bdc.md.auth.config.feign.FeignSsoLoginService;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.vo.*;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdBlackUserService;
import com.lyentech.bdc.md.auth.service.WeChatAccessTokenService;
import com.lyentech.bdc.md.auth.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.ADMIN;
import static com.lyentech.bdc.md.auth.common.constant.MdAuthRoleConstant.ANONYMOUS;
import static com.lyentech.bdc.md.auth.common.constant.MdSsoConstant.*;
import static com.lyentech.bdc.md.auth.util.IpCheckRegionUtil.getIpRegion;

/**
 * @author guolanren
 */
@Slf4j
@Service
public class MdAppServiceImpl implements MdAppService {

    private static final String LOGIN_SSO = "sso";

    private static final String LOGIN_SMS = "sms";

    private static final String LOGIN_PASSWORD = "password";

    private static final String LOGIN_SIGN = "sign";

    @Resource
    private MdAppMapper appMapper;
    @Resource
    private MdRoleMapper roleMapper;
    @Resource
    private MdMenuMapper menuMapper;
    @Resource
    private MdPermissionMapper permissionMapper;
    @Resource
    private TenantRoleMapper tenantRoleMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private MdAppSsoMapper mdAppSsoMapper;
    @Resource
    private MdAppHeadMapper mdAppHeadMapper;
    @Resource
    private UserTenantRoleMapper userTenantRoleMapper;
    @Resource
    private MdAppLoginMethodMapper loginMethodMapper;
    @Resource
    private AppTenantMapper appTenantMapper;
    @Resource
    private AppSsoMapper appSsoMapper;
    @Resource
    private FeignSsoLoginService ssoLoginService;
    @Resource
    private MdUserMapper userMapper;
    @Resource
    private MdBlackUserMapper mdBlackUserMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MdBlackUserService mdBlackUserService;
    @Resource
    private WeChatAccessTokenService weChatAccessTokenService;


    @Override
    public MdApp loadClientByClientId(String appId) throws ClientRegistrationException {
        return appMapper.getById(appId);
    }

    @Override
    public Set<TenantRoleVO> getTenantRoleInfo(String appKey) {
        Set<TenantRoleVO> tenantRoleVOS = new HashSet<>();

        List<TenantRole> appTenants = tenantRoleMapper.selectList(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getAppId, appKey));
        if (CollectionUtil.isNotEmpty(appTenants)) {
            List<Long> tenantIds = appTenants.stream()
                    .map(TenantRole::getTenantId)
                    .distinct()
                    .collect(Collectors.toList());
            Set<MdRole> roles = userTenantRoleMapper.getByAppIdAndTenantId(appKey, tenantIds);

            for (Long tenantId : tenantIds) {

                TenantRoleVO tenantRoleVO = new TenantRoleVO();
                //获取租户下的角色
                List<Long> roleIds = appTenants.stream()
                        .filter(tenantRole -> tenantId.equals(tenantRole.getTenantId()))
                        .map(TenantRole::getRoleId)
                        .collect(Collectors.toList());
                Set<MdRole> roleSet = roles.stream()
                        .filter(mdRole -> roleIds.contains(mdRole.getId()))
                        .collect(Collectors.toSet());
                Map<String, Map<String, ?>> aclInfo = getAclOldInfo(appKey, roleSet);
                tenantRoleVO.setAclInfo(aclInfo);
                tenantRoleVO.setTenantId(tenantId);
                Tenant tenant = tenantMapper.selectById(tenantId);
                tenantRoleVO.setName(tenant.getName());
                tenantRoleVOS.add(tenantRoleVO);
            }
        }

        // 系统超管不属于租户，单独处理
        MdRole mdRole = roleMapper.selectOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, ADMIN)
                .eq(MdRole::getType, 0));
        Set<String> allPermission = permissionMapper.getByAppId(appKey, null);
        Set<MdMenu> allMenu = menuMapper.getByAppId(appKey, null);
        mdRole.setPermissions(allPermission);
        mdRole.setMenus(allMenu);
        TenantRoleVO adminTenantRoleVO = getTenantRoleVO(mdRole);

        tenantRoleVOS.add(adminTenantRoleVO);

        // 匿名用户单独处理
        MdRole anonRole = new MdRole();
        Set<String> anonPermission = permissionMapper.getByAppId(appKey, 1);
        Set<MdMenu> anonMenu = menuMapper.getByAppId(appKey, 1);
        anonRole.setAppId(appKey);
        anonRole.setName(ANONYMOUS);
        anonRole.setPermissions(anonPermission);
        anonRole.setMenus(anonMenu);
        TenantRoleVO anonTenantRoleVO = getTenantRoleVO(anonRole);

        tenantRoleVOS.add(anonTenantRoleVO);

        return tenantRoleVOS;
    }

    @Override
    public Set<TenantRoleVO> getClientRoleInfo(String appKey) {
        Set<TenantRoleVO> tenantRoleVOS = new HashSet<>();

        List<TenantRole> appTenants = tenantRoleMapper.selectList(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getAppId, appKey));
        if (CollectionUtil.isNotEmpty(appTenants)) {
            List<Long> tenantIds = appTenants.stream()
                    .map(TenantRole::getTenantId)
                    .distinct()
                    .collect(Collectors.toList());
            Set<MdRole> roles = userTenantRoleMapper.getByAppIdAndTenantId(appKey, tenantIds);

            for (Long tenantId : tenantIds) {

                TenantRoleVO tenantRoleVO = new TenantRoleVO();
                //获取租户下的角色
                List<Long> roleIds = appTenants.stream()
                        .filter(tenantRole -> tenantId.equals(tenantRole.getTenantId()))
                        .map(TenantRole::getRoleId)
                        .collect(Collectors.toList());
                Set<MdRole> roleSet = roles.stream()
                        .filter(mdRole -> roleIds.contains(mdRole.getId()))
                        .collect(Collectors.toSet());
                Map<String, Map<String, ?>> aclInfo = getAclInfo(appKey, roleSet);
                tenantRoleVO.setAclInfo(aclInfo);
                tenantRoleVO.setTenantId(tenantId);
                Tenant tenant = tenantMapper.selectById(tenantId);
                tenantRoleVO.setName(tenant.getName());
                tenantRoleVOS.add(tenantRoleVO);
            }
        }

        // 系统超管不属于租户，单独处理
        MdRole mdRole = roleMapper.selectOne(Wrappers.<MdRole>lambdaQuery()
                .eq(MdRole::getAppId, appKey)
                .eq(MdRole::getName, ADMIN)
                .eq(MdRole::getType, 0));
//        Set<String> allPermission = permissionMapper.getByAppId(appKey, null);
        Set<MdMenu> allMenu = menuMapper.getByAppId(appKey, null);
//        mdRole.setPermissions(allPermission);
        mdRole.setMenus(allMenu);
        TenantRoleVO adminTenantRoleVO = getAdminRoleVO(mdRole, 0L);

        tenantRoleVOS.add(adminTenantRoleVO);

        // 匿名用户单独处
        MdRole anonRole = new MdRole();
        Set<String> anonPermission = permissionMapper.getByAppId(appKey, 1);
        Set<MdMenu> anonMenu = menuMapper.getByAppId(appKey, 1);
        anonRole.setAppId(appKey);
        anonRole.setName(ANONYMOUS);
        anonRole.setPermissions(anonPermission);
        anonRole.setMenus(anonMenu);
        TenantRoleVO anonTenantRoleVO = getAdminRoleVO(anonRole, -1L);
        tenantRoleVOS.add(anonTenantRoleVO);

        return tenantRoleVOS;
    }

    @Override
    public TenantRoleVO getTenantRoleAcl(String appKey, Long tenantId, Long roleId) {
        Set<MdRole> roleSet = userTenantRoleMapper.getByTenantIdAndRoleId(appKey, tenantId, roleId);
        Map<String, Map<String, ?>> aclInfo = getAclInfo(appKey, roleSet);
        TenantRoleVO tenantRoleVO = new TenantRoleVO();
        tenantRoleVO.setAclInfo(aclInfo);
        tenantRoleVO.setTenantId(tenantId);
        Tenant tenant = tenantMapper.selectById(tenantId);
        tenantRoleVO.setName(tenant.getName());
        return tenantRoleVO;
    }


    @Override
    public TenantRoleVO getAdminRoleAcl(String appKey, String role) {
        if (role.equals(ADMIN)) {
            // 系统超管不属于租户，单独处理
            MdRole mdRole = roleMapper.selectOne(Wrappers.<MdRole>lambdaQuery()
                    .eq(MdRole::getAppId, appKey)
                    .eq(MdRole::getName, ADMIN)
                    .eq(MdRole::getType, 0));
//        Set<String> allPermission = permissionMapper.getByAppId(appKey, null);
            Set<MdMenu> allMenu = menuMapper.getByAppId(appKey, null);
//        mdRole.setPermissions(allPermission);
            mdRole.setMenus(allMenu);
            TenantRoleVO adminTenantRoleVO = getAdminRoleVO(mdRole, 0L);
            return adminTenantRoleVO;
        } else if (role.equals(ANONYMOUS)) {
            // 系统超管不属于租户，单独处理
            MdRole anonRole = new MdRole();
            Set<String> anonPermission = permissionMapper.getByAppId(appKey, 1);
            Set<MdMenu> anonMenu = menuMapper.getByAppId(appKey, 1);
            anonRole.setAppId(appKey);
            anonRole.setName(ANONYMOUS);
            anonRole.setPermissions(anonPermission);
            anonRole.setMenus(anonMenu);
            TenantRoleVO anonTenantRoleVO = getAdminRoleVO(anonRole, -1L);
            return anonTenantRoleVO;
        }
        return null;
    }

    private Map<String, Map<String, ?>> getAclOldInfo(String appKey, Set<MdRole> roles) {
        // 创建结果类型的数据结构
        Map<String, Map<String, ?>> rpm = new HashMap<>(4);
        // 角色 -> 权限映射
        Map<String, Set> rp = new HashMap<>(16);
        // 角色 -> 菜单映射
        Map<String, Set> rm = new HashMap<>(16);
        rpm.put("permission", rp);
        rpm.put("menu", rm);

        roles.stream().forEach(role -> {
            rp.put(role.getName(), role.getPermissions());
            rm.put(role.getName(), role.getMenus());
        });

        return rpm;
    }


    private Map<String, Map<String, ?>> getAclInfo(String appKey, Set<MdRole> roles) {
        // 创建结果类型的数据结构
        Map<String, Map<String, ?>> rpm = new HashMap<>(4);
        // 角色 -> 权限映射
        Map<String, Set> rp = new HashMap<>(16);
        // 角色 -> 菜单映射
        Map<String, Set> rm = new HashMap<>(16);
        rpm.put("permission", rp);
        rpm.put("menu", rm);

        roles.stream().forEach(role -> {
            rp.put(String.valueOf(role.getId()), role.getPermissions());
            rm.put(String.valueOf(role.getId()), role.getMenus());
        });

        return rpm;
    }

    /**
     * 处理系统超管和匿名用户，因超管和匿名用户不属于租户，所以租户id设置为0
     * @param mdRole
     * @return
     */
    private TenantRoleVO getTenantRoleVO(MdRole mdRole) {
        Set<MdRole> roleSet = new HashSet<>();
        roleSet.add(mdRole);
        Map<String, Map<String, ?>> aclInfo = getAclInfo(mdRole.getAppId(), roleSet);
        TenantRoleVO tenantRoleVO = new TenantRoleVO();
        tenantRoleVO.setTenantId(0L);
        tenantRoleVO.setAclInfo(aclInfo);
        tenantRoleVO.setName("md_" + mdRole.getName());
        return tenantRoleVO;
    }
    /**
     * ArV1.8优化了处理逻辑处理系统超管和匿名用户，因超管和匿名用户不属于租户，所以租户id设置为0
     * @param mdRole
     * @return
     */
    private TenantRoleVO getAdminRoleVO(MdRole mdRole, Long tenantId) {
        Set<MdRole> roleSet = new HashSet<>();
        roleSet.add(mdRole);
        Map<String, Map<String, ?>> aclInfo = getAclInfo(mdRole.getAppId(), roleSet);
        TenantRoleVO tenantRoleVO = new TenantRoleVO();
        tenantRoleVO.setTenantId(tenantId);
        tenantRoleVO.setAclInfo(aclInfo);
        tenantRoleVO.setName("md_" + mdRole.getName());
        return tenantRoleVO;

    }

    @Override
    public boolean isAuthorize(String appKey, String appSecret) {
        ClientDetails app = loadClientByClientId(appKey);
        return app != null && app.getClientSecret().equals(appSecret);
    }

    @Override
    public String getHomepage(String appKey) {
        return appMapper.getHomepage(appKey);
    }

    /**
     * 获取负责人信息
     * @param id
     * @return
     */
    @Override
    public HeadListVO getAppInfo(HttpServletRequest request,String id) throws IOException {
        List<MdAppHead> list = mdAppHeadMapper.selectList(Wrappers.<MdAppHead>lambdaQuery().eq(MdAppHead::getAppId, id));
        HeadListVO head1 = appMapper.getHead(id);
        String appName = head1.getName();
        String homepage = head1.getHomePageUrl();
        List<AppHeadVO> headList = new ArrayList<>();
        for (MdAppHead head : list) {
            AppHeadVO vo = new AppHeadVO();
            if (ObjectUtils.isNotEmpty(head)) {
                BeanUtils.copyProperties(head, vo);
                headList.add(vo);
            }
        }
        HeadListVO vo = new HeadListVO();
        vo.setName(appName);
        vo.setHeadList(headList);
        List<String> loginTypeList = new ArrayList<>();
        MdAppLoginMethod loginMethod = loginMethodMapper.selectOne(Wrappers.<MdAppLoginMethod>lambdaQuery().eq(MdAppLoginMethod::getAppId, id));
        if (loginMethod.getCode().equals(true)) {
            loginTypeList.add(LOGIN_SMS);
        }
        if (loginMethod.getAccount().equals(true)) {
            loginTypeList.add(LOGIN_PASSWORD);
        }
        if (loginMethod.getSso().equals(true)) {
            loginTypeList.add(LOGIN_SSO);
        }
        if (loginMethod.getSign().equals(true)) {
            loginTypeList.add(LOGIN_SIGN);
        }
        vo.setLoginTypeList(loginTypeList);
        if (loginMethod.getSso().equals(true)) {
            MdAppSso sso = mdAppSsoMapper.selectOne(Wrappers.<MdAppSso>lambdaQuery().eq(MdAppSso::getAppId, id));
            if (ObjectUtils.isNotEmpty(sso)) {
                vo.setSsoAppId(sso.getSsoAppId());
            }
        }
        List<TenantVO> listByAppId = appTenantMapper.getTenantByAppId(id);
        vo.setTenantVOList(listByAppId);
        vo.setHomePageUrl(homepage);
        vo.setAppRemark(head1.getAppRemark());
        String userIp=ServletUtil.getClientIP(request);
        vo.setIsOutWeb(getIpRegion(userIp));
        return vo;
    }

    @Override
    public MdAppLoginMethod getAppLoginMethod(String id) {
        MdAppLoginMethod loginMethod = loginMethodMapper.selectOne(Wrappers.<MdAppLoginMethod>lambdaQuery().eq(MdAppLoginMethod::getAppId, id));
        return loginMethod;
    }

    @Override
    public String getSecret(String appKey) {
        return appMapper.getSecret(appKey);
    }

    @Override
    public ResultEntity appPreAuthorize(String loginType, String keyword, String appKey) {
        if (MdGrantTypeConstant.SSO.equals(loginType)) {
            //进入单点登录配置
            AppSso appSso = appSsoMapper.selectOne(Wrappers.<AppSso>lambdaQuery().eq(AppSso::getAppId, appKey));

            //第三方单点登录认证，获取单点登录token和用户信息
            Map tokenResult = ssoLoginService.getToken(keyword, appSso.getSsoAppId(), appSso.getSsoAppKey());
            if (tokenResult == null || Boolean.FALSE.equals(tokenResult.get(SSO_RESULT_SUCCESS).toString())) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getCode());
                resultEntity.setMsg(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getMessage());
                return resultEntity;
            }
            String ssoToken = tokenResult.get(SSO_RESULT_MESSAGE).toString();
            Map userResult = ssoLoginService.getUser(ssoToken, appSso.getSsoAppId(), appSso.getSsoAppKey(), null);
            if (tokenResult == null || Boolean.FALSE.equals(tokenResult.get(SSO_RESULT_SUCCESS).toString())) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getCode());
                resultEntity.setMsg(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getMessage());
                return resultEntity;
            }
            String email = userResult.get(SSO_RESULT_EMAIL).toString();
            MdUser mdUser = userMapper.searchByEmail(email);
            if (mdUser == null) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getCode());
                resultEntity.setMsg("当前邮箱号暂无权限登录本平台，请联系管理员!");
                return resultEntity;
            }
            MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getUserId, mdUser.getId()).eq(MdBlackUser::getAppId, appKey));
            if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.NOT_ROLE_LOGIN_FAIL.getCode());
                resultEntity.setMsg("用户因" + mdBlackUser.getReason()+ "无权登录系统，请联系管理员");
                return resultEntity;
            }
            return ResultEntity.success();

        } else if(MdGrantTypeConstant.SMS.equals(loginType)){
            MdUser mdUser = userMapper.searchByPhone(keyword);
            if (mdUser == null) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.SSO_LOGIN_GET_USERINFO_FAIL.getCode());
                resultEntity.setMsg("当前手机号暂无权限登录本平台，请联系管理员!");
                return resultEntity;
            }
            MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getUserId, mdUser.getId()).eq(MdBlackUser::getAppId, appKey));
            if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(ResultCodeExtend.NOT_ROLE_LOGIN_FAIL.getCode());
                resultEntity.setMsg("用户因" + mdBlackUser.getReason()+ "无权登录系统，请联系管理员");
                return resultEntity;
            }
            return ResultEntity.success();
        }else {
            return ResultEntity.success();
        }
    }

    @Override
    public CredentialSignVO credentialSign(String appKey, String message) {
        if (ObjectUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空，请重试！");
        }
        if (ObjectUtils.isEmpty(message)) {
            throw new IllegalParamException("校验信息不能为空，请重试！");
        }
        MdAppLoginMethod mdAppLoginMethod = loginMethodMapper.selectById(appKey);
        if (ObjectUtils.isEmpty(mdAppLoginMethod)) {
            throw new IllegalParamException("appKey校验失败，请重新检查!");
        } else if (mdAppLoginMethod.getSign().equals(false)){
            throw new IllegalParamException("项目不支持当前登录方式，请联系管理员处理！");
        } else {
            MdUser mdUser = userMapper.searchByEmail(message);
            if (ObjectUtils.isEmpty(mdUser)) {
                mdUser = userMapper.searchByPhone(message);
                if (ObjectUtils.isEmpty(mdUser)) {
                    throw new IllegalParamException("当前用户暂不存在，请联系管理员添加！");
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            String randomStr = createRandomStr(8);
            String credentialKey = stringBuilder.append("ar:auth:login:credential:clientId:").append(appKey).append(":").append(randomStr).toString();
            stringRedisTemplate.boundValueOps(credentialKey).set(String.valueOf(mdUser.getId()), 15, TimeUnit.MINUTES);
            CredentialSignVO credentialSignVO = new CredentialSignVO();
            credentialSignVO.setCredential(randomStr);
            credentialSignVO.setMsg("获取凭证成功，3分钟内有效");
            Long timeStamp = System.currentTimeMillis();
            credentialSignVO.setTimeStamp(timeStamp);
            return credentialSignVO;
        }
    }

    @Override
    public AuthorizationTokenVO getAuthorizationToken(String grantType, String appKey, String appSecret) {
        if (StringUtils.isEmpty(grantType) || !"client_credentials".equals(grantType)) {
            throw new IllegalParamException("请输入正确的grant_type");
        }
        if (ObjectUtils.isEmpty(appKey) || ObjectUtils.isEmpty(appSecret)) {
            throw new IllegalParamException("Ar 认证失败 ：app_key 或 appSecret 不能为空，请校验参数");
        }

        String token = JWTUtil.createToken(appKey, appSecret);
        AuthorizationTokenVO authorizationTokenVO = new AuthorizationTokenVO();
        authorizationTokenVO.setAccess_token(token);
        authorizationTokenVO.setToken_type("bearer");
        authorizationTokenVO.setExpires_in(7200);
        return authorizationTokenVO;
    }

    @Override
    public String getAppManagerSetPasswordMsg(String appKey) {
        return appMapper.getAppManagerSetPasswordMsg(appKey);
    }

    public static String createRandomStr(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }


}
