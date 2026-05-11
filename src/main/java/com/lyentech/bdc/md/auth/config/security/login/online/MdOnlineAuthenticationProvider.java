package com.lyentech.bdc.md.auth.config.security.login.online;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.md.auth.common.constant.EVNConstant;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.CUSTOMER;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.ONLINE;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;

public class MdOnlineAuthenticationProvider implements AuthenticationProvider {

    private final MdUserService userDetailsService;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private Logger log;
    private String evn;
    private RestTemplate restTemplate;

    public MdOnlineAuthenticationProvider(MdUserService userDetailsService, Logger log, String evn, RestTemplate restTemplate) {
        this.userDetailsService = userDetailsService;
        this.log = log;
        this.evn = evn;
        this.restTemplate = restTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdOnlineAuthenticationToken authenticationToken = (MdOnlineAuthenticationToken) authentication;
        String authCode = (String) ((MdOnlineAuthenticationToken) authentication).getCode();
        String clientId = (String) ((MdOnlineAuthenticationToken) authentication).getClientId();
        Long tenantId = (Long) ((MdOnlineAuthenticationToken) authentication).getTenantId();
        String loginSystem = (String) ((MdOnlineAuthenticationToken) authentication).getLoginSystem();
        String loginBrowser = (String) ((MdOnlineAuthenticationToken) authentication).getLoginBrowser();
        String userIp = (String) ((MdOnlineAuthenticationToken) authentication).getUserIp();
        Map<String, Object> map = new HashMap<>();
        Long userId=validateAuthCode(clientId, authCode);
        if (userId==null) {
            throw new BadCredentialsException("Invalid auth code");
        }
        UserDetails userDetails = userDetailsService.getById(userId.intValue());
        if(userDetails==null){
            throw new BadCredentialsException("【在线】用户尚未注册！");
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
        loginLog.setLoginWay(ONLINE);
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
        return MdOnlineAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Long validateAuthCode(String clientId, String authCode) {
        String mobile = "";
        String baseUserName = "";
        Long userId=null;
        String domain = "";
        String serviceTokenUrl = "";
        if (EVNConstant.PROD.equals(evn)) {
            domain = "https://zxkf.gree.com/";
            serviceTokenUrl = domain + "api/pub/servicebot-server-user/api/thirdParty/token";
        } else {
            domain = "http://sms-uat-test.gree.com/";
            serviceTokenUrl = domain + "api/pub/servicebot-server-user/api/thirdParty/token";
        }
        String authToken = getAuthToken(serviceTokenUrl);
        String serviceUserInfoByCodeUrl = domain + "api/sso/servicebot-server-user/api/thirdParty/getUserInfoByAuthCode";
        MdUser mdUser = null;
        if (!StringUtils.isEmpty(authToken)) {
            JSONObject userInfoByCode = getUserInfoByCode(serviceUserInfoByCodeUrl, authToken, authCode);
            if (userInfoByCode != null) {
                mobile = userInfoByCode.getString("mobile");
                baseUserName = userInfoByCode.getString("baseUserName");
                 userId =userInfoByCode.getLong("username");
                MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
                mdUser = mdUserMapper.selectOne(new QueryWrapper<MdUser>().eq("id",userId).last("limit 1"));
                if (mdUser != null) {
                    //判断该用户是否在黑名单中
                    MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
                    MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                            .eq(MdBlackUser::getUserId, mdUser.getId()));
                    if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                        throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
                    }
                } else {
                    log.info("在线组件用户联通账号:{}",baseUserName);
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
                params.add("client_secret", "9f13dc764b1f11f0a9a8fefcfe6b0909");
                params.add("grant_type", "proident");
                params.add("realm", "online-service");
                params.add("protocol", "openid-connect");
            } else {
                params.add("client_id", "knowledge_base");
                params.add("client_secret", "9f13dc764b1f11f0a9a8fefcfe6bb847");
                params.add("grant_type", "proident");
                params.add("realm", "online-service");
                params.add("protocol", "openid-connect");
            }
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 创建HttpEntity，包含请求头和请求体
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            ObjectMapper json = defaultObjectMapper();
            // 发送POST请求并接收响应
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);
            log.info("在线组件获取token请求header:{}", response.getHeaders());
            log.info("在线组件获取token返回:{}",response.getBody());
            String body = response.getBody();
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
               JSONObject result = json.readValue(body, JSONObject.class);
                if (null != result) {
                    return result.getJSONObject("data").getString("access_token");

                } else {
                    log.error("在线组件认证错误{}",body);
                    throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
                }


            } else {
                log.error("在线组件认证错误{}",body);
                throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
            }
        } catch (Exception e) {
            log.error("在线组件认证错误{}",e.getMessage());
            throw new MdLoginOutAuthErrorException("客服系统认证token失效!");
        }

    }

    private JSONObject getUserInfoByCode(String serviceUrl, String token, String code) {
        log.info("在线组件请求code码为{}",code);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> params = new HashMap<>();
        params.put("authCode", code);
        params.put("clientId", "knowledge_base");
        try {
            HttpEntity httpEntity = new HttpEntity(defaultObjectMapper().writeValueAsString(params), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, httpEntity, String.class);
            log.info("在线组件获取用户信息返回 header:{}", response.getHeaders());
            log.info("在线组件获取用户信息返回 code:{},token为{},返回结果{}",code,token,response.getBody());
            if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
                String body = response.getBody();
                JSONObject jsonObject = JSON.parseObject(body);
                if (null != jsonObject) {
                    if (200 == jsonObject.getInteger("statusCode")) {
                        return jsonObject.getJSONObject("data");
                    } else {
                        throw new MdLoginOutAuthErrorException("在线组件用户信息失败或code码失效!");
                    }
                } else {
                    throw new MdLoginOutAuthErrorException("在线组件用户信息失败或code码失效!");
                }
            } else {
                throw new MdLoginOutAuthErrorException("在线组件用户信息失败或code码失效!");
            }
        } catch (Exception e) {
            log.error("在线组件获取用户信息失败{}", e.getMessage());
            throw new MdLoginOutAuthErrorException("在线组件用户信息失败或code码失效!");
        }
    }

    private ObjectMapper defaultObjectMapper() {
        ObjectMapper json = new ObjectMapper();
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return json;
    }
}