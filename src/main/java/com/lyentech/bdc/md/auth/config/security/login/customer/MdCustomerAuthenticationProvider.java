package com.lyentech.bdc.md.auth.config.security.login.customer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.md.auth.common.constant.EVNConstant;
import com.lyentech.bdc.md.auth.common.constant.LoginResultType;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.config.security.login.sms.MdSmsAuthenticationToken;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.entity.UserChannelGroup;
import com.lyentech.bdc.md.auth.model.param.ExportUserParam;
import com.lyentech.bdc.md.auth.model.param.MdJoinParam;
import com.lyentech.bdc.md.auth.model.vo.TenantVO;
import com.lyentech.bdc.md.auth.service.MdProfileService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.TenantService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.slf4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.CUSTOMER;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.SMS;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;

public class MdCustomerAuthenticationProvider implements AuthenticationProvider {

    private final MdUserService userDetailsService;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private Logger log;
    private String evn;
    private RestTemplate restTemplate;

    public MdCustomerAuthenticationProvider(MdUserService userDetailsService, Logger log, String evn, RestTemplate restTemplate) {
        this.userDetailsService = userDetailsService;
        this.log = log;
        this.evn = evn;
        this.restTemplate = restTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdCustomerAuthenticationToken authenticationToken = (MdCustomerAuthenticationToken) authentication;
        String authCode = (String) ((MdCustomerAuthenticationToken) authentication).getCode();
        String clientId = (String) ((MdCustomerAuthenticationToken) authentication).getClientId();
        Long tenantId = (Long) ((MdCustomerAuthenticationToken) authentication).getTenantId();
        String loginSystem = (String) ((MdCustomerAuthenticationToken) authentication).getLoginSystem();
        String loginBrowser = (String) ((MdCustomerAuthenticationToken) authentication).getLoginBrowser();
        String userIp = (String) ((MdCustomerAuthenticationToken) authentication).getUserIp();
        Map<String, Object> map = new HashMap<>();
        Long userId = validateAuthCode(clientId, authCode);
        if (StringUtils.isEmpty(userId)) {
            throw new BadCredentialsException("Invalid auth code");
        }
        UserDetails userDetails = userDetailsService.getById(userId.intValue());
        if (userDetails == null) {
            throw new BadCredentialsException("【智能客服系统】用户尚未注册！");
        }
        MdUser user = (MdUser) userDetails;
        MdProfileService mdProfileService = SpringContextUtil.getBean(MdProfileService.class);
        TenantService tenantService = SpringContextUtil.getBean(TenantService.class);
        user = mdProfileService.getMe(clientId, user);
        List<TenantVO> tenantVOS = tenantService.getListByAppKeyAndUserId(clientId, user.getId());
        Map roles = mdProfileService.getRoleInfo(clientId, tenantId, user.getId());
        //添加登录日志
        MdLoginLogMapper mdLoginLogMapper = SpringContextUtil.getBean(MdLoginLogMapper.class);
        MdLoginLog loginLog = new MdLoginLog();
        loginLog.setLoginWay(CUSTOMER);
        loginLog.setOperateSystem(loginSystem);
        loginLog.setBrowser(loginBrowser);
        loginLog.setIp(userIp);
        loginLog.setAppId(clientId);
        loginLog.setAccount(user.getAccount());
        loginLog.setUserId(user.getId());
        mdLoginLogMapper.insert(loginLog);
        map.put("userInfo", user);
        map.put("userTenants", tenantVOS);
        map.put("roles", roles);
        return createSuccessAuthentication(map, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MdCustomerAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Long validateAuthCode(String clientId, String authCode) {
        String mobile = "";
        String domain = "";
        String baseUserName = "";
        Long userId = null;
        String serviceTokenUrl = "";
        if (EVNConstant.PROD.equals(evn)) {
            domain = "http://api.inner.sms.gree.com/";
            serviceTokenUrl = domain + "auth/realms/gree-shyun/protocol/openid-connect/token";
        } else {
            domain = "http://sms-uat.gree.com/";
            serviceTokenUrl = domain + "auth/realms/fdp-shyun-test/protocol/openid-connect/token";
        }
        String authToken = getAuthToken(serviceTokenUrl);
        String serviceUserInfoByCodeUrl = domain + "/api/sso/nts-foundation-flycloud-user/api/v1/user/info/by-auth-code";
        MdUser mdUser = null;
        if (!StringUtils.isEmpty(authToken)) {
            JSONObject userInfoByCode = getUserInfoByCode(serviceUserInfoByCodeUrl, authToken, authCode);
            if (userInfoByCode != null) {
                mobile = userInfoByCode.getString("mobile");
                baseUserName = userInfoByCode.getString("baseUserName");
                userId = userInfoByCode.getLong("username");
                MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
                mdUser = mdUserMapper.selectOne(new QueryWrapper<MdUser>().eq("id", userId).last("limit 1"));
                if (mdUser != null) {
                    //判断该用户是否在黑名单中
                    MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
                    MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                            .eq(MdBlackUser::getUserId, mdUser.getId()));
                    if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                        throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
                    }
                } else {
                    log.info("热线平台联通账号:{}", baseUserName);
                    throw new MdLoginOutAuthErrorException("用户不存在！");
                }
            }
        } else {
            throw new MdLoginOutAuthErrorException("SMS系统授权失败，Code码无效！");
        }
        if (mdUser == null) {
            throw new MdLoginOutAuthErrorException("SMS系统授权失败！");
        }
        StringBuilder authBuilder = new StringBuilder();
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(mdUser.getId()).append(":")
                .append("client:").append(clientId).toString();
        //删除用户权限变更拦截
        stringRedisTemplate.delete(authKey);
        //删除管理员重置密码拦截
        stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
        return userId;
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
                params.add("client_id", "knowledge_base");
                params.add("client_secret", "15441a4c-22bb-460e-bcd9-c09e46ac7814");
                params.add("grant_type", "client_credentials");
            } else {
                params.add("client_id", "knowledge_base");
                params.add("client_secret", "1db56e83-aa0d-4527-867f-44b8dac732a2");
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
            log.info("热线平台获取token请求header:{}", JSONObject.toJSON(params), response.getHeaders().getFirst("X-Request-ID"));
            log.info("热线平台获取token返回:{}", response.getBody());
            String body = response.getBody();
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                Map<String, String> result = json.readValue(body, Map.class);
                if (null != result) {
                    return result.get("access_token");

                } else {
                    log.error("热线工作台认证错误{}", body);
                    throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
                }


            } else {
                log.error("热线工作台认证错误{}", body);
                throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
            }
        } catch (Exception e) {
            log.error("热线工作台认证错误{}", e.getMessage());
            throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
        }

    }

    private JSONObject getUserInfoByCode(String serviceUrl, String token, String code) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> params = new HashMap<>();
        params.put("authCode", code);
        try {
            HttpEntity httpEntity = new HttpEntity(defaultObjectMapper().writeValueAsString(params), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, httpEntity, String.class);
            log.info("请求code码为{}", code);
            log.info("热线平台获取用户信息请求header:{},请求requestId为{}", JSONObject.toJSON(params), response.getHeaders().getFirst("X-Request-ID"));
            log.info("热线平台获取用户信息返回 code:{},token为{},返回结果{}", code, token, response.getBody());
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                String body = response.getBody();
                JSONObject jsonObject = JSON.parseObject(body);
                if (null != jsonObject) {
                    if (200 == jsonObject.getInteger("statusCode")) {
                        return jsonObject.getJSONObject("data");
                    } else {
                        throw new MdLoginOutAuthErrorException("客服系统用户信息失败或code码失效!");
                    }
                } else {
                    throw new MdLoginOutAuthErrorException("客服系统用户信息失败或code码失效!");
                }
            } else {
                throw new MdLoginOutAuthErrorException("客服系统用户信息失败或code码失效!");
            }
        } catch (Exception e) {
            log.error("SMS系统获取用户信息失败{}", e.getMessage());
            throw new MdLoginOutAuthErrorException("客服系统用户信息失败或code码失效!");
        }
    }

    private ObjectMapper defaultObjectMapper() {
        ObjectMapper json = new ObjectMapper();
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return json;
    }
}