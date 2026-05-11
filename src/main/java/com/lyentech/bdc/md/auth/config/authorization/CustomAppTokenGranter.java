package com.lyentech.bdc.md.auth.config.authorization;

import com.lyentech.bdc.md.auth.common.constant.MdGrantTypeConstant;
import com.lyentech.bdc.md.auth.config.security.login.sms.MdSmsAuthenticationToken;
import com.lyentech.bdc.md.auth.config.security.login.sso.MdSsoAuthenticationToken;
import com.lyentech.bdc.md.auth.config.security.login.wechat.MdWeChatAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author YuYi
 * @create 2023/6/2
 * @create 16:41
 * 此处自定义了Oauth 生成CustomAppToken的逻辑
 */
public class CustomAppTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "app_token";

    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(CustomAppTokenGranter.class);

    public CustomAppTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices
                                , ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected CustomAppTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
                                    OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String grantType = parameters.get("app_grand_type");
        String appKey = tokenRequest.getClientId();
        Authentication userAuth = null;

        if (MdGrantTypeConstant.SMS.equals(grantType)) {
            String phone = parameters.get("phone");
            String smsCode = parameters.get("sms_code");
            if (phone == null || phone.trim().isEmpty()) {
                throw new InvalidGrantException("手机号不能为空");
            }
            if (smsCode == null || smsCode.trim().isEmpty()) {
                throw new InvalidGrantException("验证码不能为空");
            }
            userAuth = new MdSmsAuthenticationToken(phone, smsCode, appKey, null, null, null);
            userAuth = authenticateUser(userAuth, "SMS");
        } else if (MdGrantTypeConstant.SSO.equals(grantType)) {
            String callback = parameters.get("callback");
            if (callback == null || callback.trim().isEmpty()) {
                throw new InvalidGrantException("回调地址不能为空");
            }
            userAuth = new MdSsoAuthenticationToken(callback, appKey);
            userAuth = authenticateUser(userAuth, "SSO");
        } else if (MdGrantTypeConstant.WECHAT.equals(grantType)) {
            String wechatCode = parameters.get("wechatCode");
            if (wechatCode == null || wechatCode.trim().isEmpty()) {
                throw new InvalidGrantException("微信授权码不能为空");
            }
            logger.info("用户进入微信登录, appKey: {}", appKey);
            userAuth = new MdWeChatAuthenticationToken(appKey, wechatCode, null, null, null);
            userAuth = authenticateUser(userAuth, "WECHAT");
        } else {
            logger.warn("不支持的授权类型: {}", grantType);
            throw new InvalidGrantException("不支持的授权类型");
        }

        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("验证码输入不正确或已过期");
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

    /**
     * 执行认证并处理异常
     *
     * @param userAuth 认证Token
     * @param authType 认证类型（用于日志）
     * @return 认证后的Authentication对象
     * @throws InvalidGrantException 认证失败时抛出
     */
    private Authentication authenticateUser(Authentication userAuth, String authType) {
        try {
            return authenticationManager.authenticate(userAuth);
        } catch (AuthenticationException e) {
            logger.warn("{}认证失败: {}", authType, e.getMessage());
            throw new InvalidGrantException("认证失败: " + e.getMessage());
        }
    }
}
