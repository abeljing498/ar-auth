package com.lyentech.bdc.md.auth.config.security.login.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.common.constant.LoginResultType;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSmsErrorException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSsoErrorException;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import com.lyentech.bdc.md.auth.service.WeChatAccessTokenService;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.WECHAT;

/**
 * @author guolanren
 */

public class MdWeChatAuthenticationProvider implements AuthenticationProvider {

    private MdUserService userService;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private MdBlackUserMapper blackUserMapper;
    private Logger log;
    private String wechatUrl;

    public MdWeChatAuthenticationProvider(MdUserService userService, MdBlackUserMapper blackUserMapper, Logger log, String wechatUrl) {
        this.userService = userService;
        this.blackUserMapper = blackUserMapper;
        this.log = log;
        this.wechatUrl=wechatUrl;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        MdWeChatAuthenticationToken authenticationToken = (MdWeChatAuthenticationToken) authentication;
        log.info("进入微信认证{}",JSON.toJSONString(authenticationToken));
        String clientId = authenticationToken.getClientId();
        String loginBrowser = authenticationToken.getLoginBrowser();
        String userIp = authenticationToken.getUserIp();
        String loginSystem = authenticationToken.getLoginSystem();
        String phone = "";
        MdLoginLogMapper mdLoginLogMapper = SpringContextUtil.getBean(MdLoginLogMapper.class);
        try {
        WeChatAccessTokenService weChatAccessTokenService = SpringContextUtil.getBean(WeChatAccessTokenService.class);
        RestTemplate restTemplate = SpringContextUtil.getBean(RestTemplate.class);

        String wechatCode = (String) authenticationToken.getPrincipal();

        String accessToken = weChatAccessTokenService.getAccessToken();

        if (accessToken == null) {
            log.error("获取微信AccessToken失败");
            throw new MdLoginSsoErrorException("获取微信AccessToken失败");
        }
        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("code", wechatCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        int retryCount = 0;
        final int MAX_RETRY = 1;

        while (retryCount <= MAX_RETRY) {
            String url = wechatUrl+"wxa/business/getuserphonenumber?access_token=" + accessToken;
            log.info("请求微信URL{}",url);
            String responseStr = restTemplate.postForObject(
                    url,
                    new HttpEntity<>(request, headers),
                    String.class
            );
            log.info("获取微信手机号码回调{}", responseStr);

            if (responseStr != null) {
                JSONObject response = JSON.parseObject(responseStr);
                Integer errcode = response.getInteger("errcode");

                // errcode为0表示成功
                if (errcode == null || errcode == 0) {
                    JSONObject phoneInfo = response.getJSONObject("phone_info");
                    if (phoneInfo != null) {
                        phone = phoneInfo.getString("phoneNumber");
                    }
                    break;
                }

                // 40001或42001表示token无效或过期，需要刷新token重试
                if ((errcode == 42001 || errcode == 40001) && retryCount < MAX_RETRY) {
                    log.warn("微信AccessToken已过期(errcode={})，正在刷新token重试", errcode);
                    accessToken = weChatAccessTokenService.refreshAccessToken();
                    if (accessToken == null) {
                        log.error("刷新微信AccessToken失败");
                        break;
                    }
                    retryCount++;
                    continue;
                }
            }
            break;
        }
        } catch (Exception e) {
            log.info("调用微信获取手机号API失败{}", e.getMessage());
            throw new MdLoginSsoErrorException("获取微信AccessToken失败");
        }
        MdLoginLog loginLog = new MdLoginLog();
        loginLog.setLoginWay(WECHAT);
        loginLog.setOperateSystem(loginSystem);
        loginLog.setBrowser(loginBrowser);
        loginLog.setIp(userIp);
        loginLog.setAppId(clientId);
        if (StringUtils.isEmpty(phone)) {
            throw new MdLoginSsoErrorException("获取微信AccessToken失败");
        }
        // 手机号未注册则登录失败
        MdUser userDetails = userService.getByPhone(phone);
        if (userDetails == null) {
            loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
            loginLog.setAccount(phone);
            loginLog.setFailReason("用户不存在");
            mdLoginLogMapper.insert(loginLog);
            throw new MdLoginSmsErrorException("用户不存在！");
        }
        MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
        MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                .eq(MdBlackUser::getUserId, userDetails.getId()));
        if (ObjectUtils.isNotEmpty(mdBlackUser)) {
            loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
            loginLog.setAccount(phone);
            loginLog.setFailReason("用户为黑名单用户！");
            mdLoginLogMapper.insert(loginLog);
            throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
        }
        return createSuccessAuthentication(userDetails, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdWeChatAuthenticationToken.class
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
