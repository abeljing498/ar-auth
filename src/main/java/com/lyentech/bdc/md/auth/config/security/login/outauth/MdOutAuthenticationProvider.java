package com.lyentech.bdc.md.auth.config.security.login.outauth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.md.auth.common.constant.EVNConstant;
import com.lyentech.bdc.md.auth.common.constant.SMSType;
import com.lyentech.bdc.md.auth.config.feign.FeignSsoLoginService;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.dao.AppSsoMapper;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.AppSso;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
import com.lyentech.bdc.md.auth.model.param.AddUserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.param.MdJoinParam;
import com.lyentech.bdc.md.auth.model.param.OrgUserParam;
import com.lyentech.bdc.md.auth.service.MdUserChannelService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.util.CharacterUntil;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.*;

import static com.lyentech.bdc.md.auth.common.constant.MdSsoConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;

/**
 * @author guolanren
 */

public class MdOutAuthenticationProvider implements AuthenticationProvider {

    private MdUserService userService;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private OrgUserService orgUserService;
    private RestTemplate restTemplate;
    private MdBlackUserMapper blackUserMapper;
    private String authAutoRegistAppId;
    private MdUserChannelService mdUserChannelService;
    private Logger log;
    private String evn;

    public MdOutAuthenticationProvider(MdUserService userService, OrgUserService orgUserService, RestTemplate restTemplate, MdBlackUserMapper blackUserMapper, String authAutoRegistAppId, Logger log, String evn, MdUserChannelService mdUserChannelService) {
        this.userService = userService;
        this.orgUserService = orgUserService;
        this.restTemplate = restTemplate;
        this.blackUserMapper = blackUserMapper;
        this.authAutoRegistAppId = authAutoRegistAppId;
        this.log = log;
        this.evn = evn;
        this.mdUserChannelService = mdUserChannelService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdOutAuthenticationToken authenticationToken = (MdOutAuthenticationToken) authentication;
        String callback = (String) authenticationToken.getPrincipal();
        String clientId = authenticationToken.getClientId();
        String outCode = callback;
        String domain = "";
        String serviceTokenUrl="";
        if (EVNConstant.PROD.equals(evn)) {
            domain = "http://api.inner.sms.gree.com/";
            serviceTokenUrl = domain + "auth/realms/gree-shyun/protocol/openid-connect/token";
        } else {
            domain = "http://sms-uat.gree.com/";
            serviceTokenUrl = domain + "auth/realms/fdp-shyun-test/protocol/openid-connect/token";
        }

        String serviceUserInfoByCodeUrl = domain + "api/sso/nts-foundation-flycloud-user/api/v1/oauth2/user";
        String authToken = getAuthToken(serviceTokenUrl);
        MdUser mdUser = null;
        if (!StringUtils.isEmpty(authToken)) {
            JSONObject userInfoByCode = getUserInfoByCode(serviceUserInfoByCodeUrl, authToken, outCode);
            if (userInfoByCode != null) {
                String userName = userInfoByCode.getString("username");
                String realName = userInfoByCode.getString("realName");
                MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
                mdUser = mdUserMapper.searchByAccount("sms_" + userName);
                if (mdUser != null) {
                    //判断该用户是否在黑名单中
                    MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
                    MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                            .eq(MdBlackUser::getUserId, mdUser.getId()));
                    if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                        throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
                    }
                } else {
                    MdJoinParam mdJoinParam = new MdJoinParam();
                    mdJoinParam.setNickname(realName);
                    mdJoinParam.setAppId(clientId.toLowerCase());
                    mdJoinParam.setAccount("sms_" + userName);
                    mdUser = userService.registerByAccount(mdJoinParam);
                }
            }
        } else {
            throw new MdLoginOutAuthErrorException("SMS系统授权失败，Code码无效！");
        }
        if (mdUser == null) {
            throw new MdLoginOutAuthErrorException("SMS系统授权失败！");
        }
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
        return (MdOutAuthenticationToken.class
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

    private String getAuthToken(String serviceUrl) {
        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            if (EVNConstant.PROD.equals(evn)) {
                params.add("client_id", "qa-client");
                params.add("client_secret", "41fa0d81-96ac-4185-8872-841a0a6d2e4e");
                params.add("grant_type", "client_credentials");
            } else {
                params.add("client_id", "qa-client");
                params.add("client_secret", "67089c7f-c037-4bf6-9218-51e6dff0675e");
                params.add("grant_type", "client_credentials");
            }
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 创建HttpEntity，包含请求头和请求体
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            ObjectMapper json = defaultObjectMapper();
            // 发送POST请求并接收响应
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);
            String body = response.getBody();
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                log.info(body);
                Map<String, String> result = json.readValue(body, Map.class);
                if (null != result) {
                    return result.get("access_token");

                } else {
                    log.error(body);
                    throw new MdLoginOutAuthErrorException("获取第三方认证token失效!");
                }


            } else {
                log.error(body);
                throw new MdLoginOutAuthErrorException("获取第三方认证token失效!");
            }
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new MdLoginOutAuthErrorException("获取第三方认证token失效!");
        }

    }
    private JSONObject getUserInfoByCode(String serviceUrl, String token, String code) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.set("code", code);
            HttpEntity httpEntity = new HttpEntity(headers);
            // 向资源服务器请求用户信息修改
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(serviceUrl)
                    .queryParams(params)
                    .build()
                    .encode()
                    .toUri();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, // 带有查询参数的URL
                    HttpMethod.GET, // 请求方法
                    httpEntity, // 包含请求头的HttpEntity对象
                    String.class // 响应体的类型
            );
           log.info(response.getBody());
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                String body = response.getBody();
                JSONObject jsonObject = JSON.parseObject(body);
                if (null != jsonObject) {
                    if (200 == jsonObject.getInteger("statusCode")) {
                        return jsonObject.getJSONObject("data");
                    } else {
                        throw new MdLoginOutAuthErrorException("获取第三方用户信息失败或code码失效!");
                    }
                } else {
                    throw new MdLoginOutAuthErrorException("获取第三方用户信息失败或code码失效!");
                }
            } else {
                throw new MdLoginOutAuthErrorException("获取第三方用户信息失败或code码失效!");
            }
        } catch (Exception e) {
            log.error("SMS系统获取用户信息失败{}",e.getMessage());
            throw new MdLoginOutAuthErrorException("获取第三方用户信息失败或code码失效!");
        }
    }

    private ObjectMapper defaultObjectMapper() {
        ObjectMapper json = new ObjectMapper();
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return json;
    }
}
