package com.lyentech.bdc.md.auth.config.security.login.smscus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.md.auth.common.constant.EVNConstant;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdJoinParam;
import com.lyentech.bdc.md.auth.service.MdUserChannelService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.slf4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthTokenConstant.FAQC_TOKEN_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;

/**
 * @author guolanren
 */

public class MdSmsCusAuthenticationProvider implements AuthenticationProvider {
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private Logger log;
    private String evn;

    public MdSmsCusAuthenticationProvider(Logger log, String evn) {
        this.log = log;
        this.evn = evn;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        MdSmsCusAuthenticationToken authenticationToken = (MdSmsCusAuthenticationToken) authentication;
        String faqcToken = (String) authenticationToken.getPrincipal();
        String appKey = (String) authenticationToken.getClientId();
        log.info("用户进入组件鉴权模块token为{}",faqcToken);
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        if (!stringRedisTemplate.hasKey(FAQC_TOKEN_PATH + ":" + faqcToken)) {
            throw new MdLoginOutAuthErrorException("用户身份已过期！");
        }
        String strUserId = stringRedisTemplate.opsForValue().get(FAQC_TOKEN_PATH + ":" + faqcToken);
        if (StringUtils.isEmpty(strUserId)) {
            throw new MdLoginOutAuthErrorException("用户身份已过期！");
        }
        Long userId = Long.valueOf(strUserId);
        MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
        MdUser mdUser = mdUserMapper.selectById(userId);
        //判断该用户是否在黑名单中
        MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
        MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, appKey)
                .eq(MdBlackUser::getUserId, mdUser.getId()));
        if (ObjectUtils.isNotEmpty(mdBlackUser)) {
            throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
        }
        UserDetails userDetails = mdUser;
        StringBuilder authBuilder = new StringBuilder();
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(mdUser.getId()).append(":")
                .append("client:").append(appKey).toString();
        //删除用户权限变更拦截
        stringRedisTemplate.delete(authKey);
        //删除管理员重置密码拦截
        stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
        return createSuccessAuthentication(userDetails, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdSmsCusAuthenticationToken.class
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
