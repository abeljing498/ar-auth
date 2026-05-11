package com.lyentech.bdc.md.auth.config.security.login.unicom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.md.auth.common.constant.EVNConstant;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.config.security.login.smscus.MdSmsCusAuthenticationToken;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
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

import static com.lyentech.bdc.md.auth.common.constant.MdAuthTokenConstant.FAQC_TOKEN_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;

/**
 * @author guolanren
 */

public class MdUnicomAuthenticationProvider implements AuthenticationProvider {
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private Logger log;
    private String evn;

    public MdUnicomAuthenticationProvider(Logger log, String evn) {
        this.log = log;
        this.evn = evn;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdUnicomAuthenticationToken authenticationToken = (MdUnicomAuthenticationToken) authentication;
        String faqcToken = (String) authenticationToken.getPrincipal();
        String appKey = (String) authenticationToken.getClientId();
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        log.info("联通统一认证token为{}", faqcToken);
        JSONObject result = getUnicomUserInfo(faqcToken, evn);
        if (result != null) {
            Long userId = result.getLong("qaUsername");
            MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
            MdUser mdUser = mdUserMapper.selectById(userId);
            if (mdUser != null) {
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

            } else {
                throw new MdLoginOutAuthErrorException("此用户在智能问答系统中不存在！");
            }
        } else {
            throw new MdLoginOutAuthErrorException("用户身份已过期！");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdUnicomAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

    private JSONObject getUnicomUserInfo(String token, String evn) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.set("token", token);
            HttpEntity httpEntity = new HttpEntity(headers);
            String domain = "";
            if (EVNConstant.PROD.equals(evn)) {
                domain = "http://faqc.gree.com/";
            } else {
                domain = "http://faqc-test.leayun.cn/";
            }
            // 向资源服务器请求用户信息修改
            String serviceUrl = domain + "currentUser";
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(serviceUrl)
                    .queryParams(params)
                    .build()
                    .encode()
                    .toUri();
            RestTemplate restTemplate = SpringContextUtil.getBean(RestTemplate.class);
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, // 带有查询参数的URL
                    HttpMethod.GET, // 请求方法
                    httpEntity, // 包含请求头的HttpEntity对象
                    String.class // 响应体的类型
            );
            log.info("调用联通获取用户信息返回token为{},结果为{}",token, response.getBody());
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                String body = response.getBody();
                JSONObject jsonObject = JSON.parseObject(body);
                if (null != jsonObject) {
                    if (200 == jsonObject.getInteger("code")) {
                        return jsonObject.getJSONObject("data");
                    } ;
                } else {
                    return null;
                }


            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
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
