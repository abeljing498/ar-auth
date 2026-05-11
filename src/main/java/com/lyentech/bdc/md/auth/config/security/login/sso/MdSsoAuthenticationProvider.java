package com.lyentech.bdc.md.auth.config.security.login.sso;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.config.feign.FeignSsoLoginService;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSsoErrorException;
import com.lyentech.bdc.md.auth.dao.AppSsoMapper;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.model.entity.AppSso;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdJoinParam;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.util.CharacterUntil;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.slf4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdSsoConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.*;

/**
 * @author guolanren
 */

public class MdSsoAuthenticationProvider implements AuthenticationProvider {

    private MdUserService userService;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private FeignSsoLoginService ssoLoginService;
    private AppSsoMapper appSsoMapper;
    private MdBlackUserMapper blackUserMapper;
    private String authAutoRegistAppId;
    private Logger log;

    public MdSsoAuthenticationProvider(MdUserService userService, FeignSsoLoginService ssoLoginService, AppSsoMapper appSsoMapper, MdBlackUserMapper blackUserMapper, String authAutoRegistAppId, Logger log) {
        this.userService = userService;
        this.ssoLoginService = ssoLoginService;
        this.appSsoMapper = appSsoMapper;
        this.blackUserMapper = blackUserMapper;
        this.authAutoRegistAppId = authAutoRegistAppId;
        this.log = log;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdSsoAuthenticationToken authenticationToken = (MdSsoAuthenticationToken) authentication;

        String callback = (String) authenticationToken.getPrincipal();
        String clientId = authenticationToken.getClientId();
        AppSso appSso = appSsoMapper.selectOne(Wrappers.<AppSso>lambdaQuery().eq(AppSso::getAppId, clientId));

        //第三方单点登录认证，获取单点登录token和用户信息
        Map tokenResult = ssoLoginService.getToken(callback, appSso.getSsoAppId(), appSso.getSsoAppKey());
        if (tokenResult == null || Boolean.FALSE.equals(tokenResult.get(SSO_RESULT_SUCCESS).toString())) {
            throw new MdLoginSsoErrorException("单点登录token获取失败");
        }
        String ssoToken = tokenResult.get(SSO_RESULT_MESSAGE).toString();
        Map userResult = ssoLoginService.getUser(ssoToken, appSso.getSsoAppId(), appSso.getSsoAppKey(), null);
        if (tokenResult == null || Boolean.FALSE.equals(tokenResult.get(SSO_RESULT_SUCCESS).toString())) {
            throw new MdLoginSsoErrorException("单点登录用户信息获取失败");
        }
        log.info("SSO回调用户信息{}", JSON.toJSONString(userResult));
        String email = "";
        if(null!=userResult.get(SSO_RESULT_EMAIL)){
            email= userResult.get(SSO_RESULT_EMAIL).toString();
        }
        String empId = "";
        if (null != userResult.get(SSO_RESULT_EMPID)) {
             empId = userResult.get(SSO_RESULT_EMPID).toString();
        }
        if (null != userResult.get(SSO_RESULT_HREMPID)) {
             empId = userResult.get(SSO_RESULT_HREMPID).toString();
        }
        String userName = userResult.get(SSO_RESULT_USERNAME).toString();
        // 邮箱号不存在则登录失败
        MdUser mdUser = null;
        if (!StringUtils.isEmpty(email)) {
            email = CharacterUntil.emailToChange(email);
            mdUser = userService.getByEmail(email);
            if (mdUser == null) {
                MdJoinParam mdJoinParam = new MdJoinParam();
                mdJoinParam.setNickname(userName);
                mdJoinParam.setAppId(clientId.toLowerCase());
                mdJoinParam.setEmail(email);
                mdJoinParam.setEmployeeId(empId);
                mdUser = userService.registerByEmail(mdJoinParam);
            } else {
                mdUser.setEmployeeId(empId);
                userService.updateById(mdUser);
            }
        } else {
            mdUser = userService.getEmployeeId(empId);
            if (mdUser == null) {
                MdJoinParam mdJoinParam = new MdJoinParam();
                mdJoinParam.setNickname(userName);
                mdJoinParam.setAppId(clientId.toLowerCase());
                mdJoinParam.setEmail(email);
                mdJoinParam.setEmployeeId(empId);
                mdUser = userService.registerByEmail(mdJoinParam);
            }
        }
        if (mdUser == null) {
            throw new MdLoginSsoErrorException("用户信息有误，请确认后再登录！");
        }
        MdBlackUser mdBlackUser = blackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                .eq(MdBlackUser::getUserId, mdUser.getId()));
        if (ObjectUtils.isNotEmpty(mdBlackUser)) {
            //清除单点登录的登录态
            if (!StringUtils.isEmpty(ssoToken)) {
                AppSso appSso1 = appSsoMapper.selectOne(Wrappers.<AppSso>lambdaQuery().eq(AppSso::getAppId, clientId));
                Map map = ssoLoginService.loginOut(ssoToken, appSso1.getSsoAppId(), appSso1.getSsoAppKey());
            }
            throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "被加入黑名单，暂无权限进入当前系统");
        }
        mdUser.setSsoToken(ssoToken);
        UserDetails userDetails = mdUser;

        StringBuilder authBuilder = new StringBuilder();
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(mdUser.getId()).append(":")
                .append("client:").append(clientId).toString();
        //删除用户权限变更拦截
        stringRedisTemplate.delete(authKey);
        //删除管理员重置密码拦截
        stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
        return createSuccessAuthentication(userDetails, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdSsoAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal, authentication.getCredentials(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());

        return result;
    }
}
