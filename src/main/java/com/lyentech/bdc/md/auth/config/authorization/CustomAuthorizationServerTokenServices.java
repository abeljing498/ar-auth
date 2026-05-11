package com.lyentech.bdc.md.auth.config.authorization;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.model.entity.MdApp;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.*;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YuYi
 * @create 2023/4/16
 * @create 15:17
 */
public class CustomAuthorizationServerTokenServices extends DefaultTokenServices {

    private int refreshTokenValiditySeconds = 60 * 60 * 24 * 30; // default 30 days.

    private int accessTokenValiditySeconds = 60 * 60 * 12; // default 12 hours.

    // ~ Necessary
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 自定义的持久化令牌的接口 {@link TokenStore} 引用
     */
    private final TokenStore tokenStore;

    /**
     * 自定义的 {@link ClientDetailsService} 的引用
     */
    private final ClientDetailsService clientDetailsService;


    // ~ Optional
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@link AuthenticationManager}
     */
    private AuthenticationManager authenticationManager;


    // =================================================================================================================

    /**
     * {@link TokenEnhancer}
     */
    private TokenEnhancer accessTokenEnhancer;

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    /**
     * Description: 构建 {@link AuthorizationServerTokenServices}<br>
     * Details: 依赖 {@link TokenStore}, {@link ClientDetailsService}\
     *
     * @param endpoints {@link AuthorizationServerEndpointsConfigurer}
     * @author LiKe
     * @date 2020-07-08 15:24:18
     */
    public CustomAuthorizationServerTokenServices(AuthorizationServerEndpointsConfigurer endpoints) {
        this.tokenStore = Objects.requireNonNull(endpoints.getTokenStore(), "tokenStore 不能为空!");
        this.clientDetailsService = Objects.requireNonNull(endpoints.getClientDetailsService(), "clientDetailsService 不能为空!");
    }

    /**
     * 创建 access-token
     * <p>
     * //     * @see org.springframework.security.oauth2.provider.token.DefaultTokenServices#createAccessToken(OAuth2Authentication)
     */
    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
        // 当前客户端是否支持 refresh_token
        final boolean supportRefreshToken = isSupportRefreshToken(authentication);
        OAuth2RefreshToken existingRefreshToken = null;
        // 如果已经存在令牌
        final OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
        if (Objects.nonNull(existingAccessToken)) {
            if (existingAccessToken.isExpired()) {
                // 如果已过期, 则删除 AccessToken 和 RefreshToken
                if (supportRefreshToken) {
                    existingRefreshToken = existingAccessToken.getRefreshToken();
                    tokenStore.removeRefreshToken(existingRefreshToken);
                }
                tokenStore.removeAccessToken(existingAccessToken);
            } else {
                String clientId = authentication.getOAuth2Request().getClientId();
                MdApp mdApp = (MdApp) clientDetailsService.loadClientByClientId(clientId);
                if (mdApp.getLoginKick() == 0) {
                    tokenStore.storeAccessToken(existingAccessToken, authentication);
                    return existingAccessToken;
                } else {
                    //此处重写了登录逻辑，加入了多端互踢
                    StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
                    String existToken = existingAccessToken.getValue();
                    String tokenKey = "MD_INVALID_TOKEN:" + existToken;
                    stringRedisTemplate.boundValueOps(tokenKey).set("TOKEN_INVALID", 2, TimeUnit.HOURS);
                    tokenStore.removeAccessToken(existingAccessToken);
                    SendMessageToWebSocketService sendMessageToWebSocketService = SpringContextUtil.getBean(SendMessageToWebSocketService.class);
                    //通知下游进行登出操作
                    PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
                    pushAppOrderMsgDto.setAppKey(clientId);
                    pushAppOrderMsgDto.setToken(existToken);
                    pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.INVALID_TOKEN);
                    sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
                }
            }
        }

        // 生成新的 refresh_token
        OAuth2RefreshToken newRefreshToken = null;
        if (supportRefreshToken) {
            if (Objects.isNull(existingRefreshToken)) {
                // 如果没有 RefreshToken, 生成一个
                newRefreshToken = tokenGenerator.createRefreshToken(authentication);
            } else if (existingRefreshToken instanceof ExpiringOAuth2RefreshToken) {
                // 如果有 RefreshToken 但是已经过期, 重新颁发
                if (System.currentTimeMillis() > ((ExpiringOAuth2RefreshToken) existingRefreshToken).getExpiration().getTime()) {
                    newRefreshToken = tokenGenerator.createRefreshToken(authentication);
                }
            }
        }

        // 生成新的 access_token
        final OAuth2AccessToken newAccessToken = tokenGenerator.createAccessToken(authentication, newRefreshToken);
        if (supportRefreshToken) {
            tokenStore.storeRefreshToken(newRefreshToken, authentication);
        }
        tokenStore.storeAccessToken(newAccessToken, authentication);

        return newAccessToken;
    }

    /**
     * 刷新 access-token
     *
     * @see DefaultTokenServices#refreshAccessToken(String, TokenRequest)
     */
    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
        final String clientId = tokenRequest.getClientId();
        if (Objects.isNull(clientId) || !StringUtils.equals(clientId, tokenRequest.getClientId())) {
            throw new InvalidGrantException(String.format("错误的客户端: %s, refresh token: %s", clientId, refreshTokenValue));
        }

        if (!isSupportRefreshToken(clientId)) {
            throw new InvalidGrantException(String.format("客户端 (%s) 不支持 refresh_token!", clientId));
        }

        final OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (Objects.isNull(refreshToken)) {
            throw new InvalidTokenException(String.format("无效的 refresh_token: %s!", refreshTokenValue));
        }

        // ~ 用 refresh_token 获取 OAuth2 认证信息
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        if (Objects.nonNull(this.authenticationManager) && !oAuth2Authentication.isClientOnly()) {
            oAuth2Authentication = new OAuth2Authentication(
                    oAuth2Authentication.getOAuth2Request(),
                    authenticationManager.authenticate(
                            new PreAuthenticatedAuthenticationToken(oAuth2Authentication.getUserAuthentication(), "", oAuth2Authentication.getAuthorities())
                    )
            );
            oAuth2Authentication.setDetails(oAuth2Authentication.getDetails());
        }

        tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);

        if (isExpired(refreshToken)) {
            tokenStore.removeRefreshToken(refreshToken);
            throw new InvalidTokenException("无效的 refresh_token (已过期)!");
        }

        // ~ 刷新 OAuth2 认证信息, 并基于此构建新的 OAuth2AccessToken
        oAuth2Authentication = createRefreshedAuthentication(oAuth2Authentication, tokenRequest);
        // 获取新的 refresh_token
        final OAuth2AccessToken refreshedAccessToken = tokenGenerator.createAccessToken(oAuth2Authentication, refreshToken);
        tokenStore.storeAccessToken(refreshedAccessToken, oAuth2Authentication);
        return refreshedAccessToken;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        return tokenStore.getAccessToken(authentication);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Description: 判断当前客户端是否支持 refreshToken
     *
     * @param authentication {@link OAuth2Authentication}
     * @return boolean
     * @author LiKe
     * @date 2020-07-08 18:16:09
     */
    private boolean isSupportRefreshToken(OAuth2Authentication authentication) {
        return isSupportRefreshToken(authentication.getOAuth2Request().getClientId());
    }

    /**
     * Description: 判断当前客户端是否支持 refreshToken
     *
     * @param clientId 客户端 ID
     * @return boolean
     * @author LiKe
     * @date 2020-07-09 10:02:11
     */
    private boolean isSupportRefreshToken(String clientId) {
        return clientDetailsService.loadClientByClientId(clientId).getAuthorizedGrantTypes().contains("refresh_token");
    }

    /**
     * Create a refreshed authentication.<br>
     * <i>(Copied from DefaultTokenServices#createRefreshedAuthentication(OAuth2Authentication, TokenRequest))</i>
     *
     * @param authentication The authentication.
     * @param tokenRequest   The scope for the refreshed token.
     * @return The refreshed authentication.
     * @throws InvalidScopeException If the scope requested is invalid or wider than the original scope.
     */
    private OAuth2Authentication createRefreshedAuthentication(OAuth2Authentication authentication, TokenRequest tokenRequest) {
        Set<String> tokenRequestScope = tokenRequest.getScope();
        OAuth2Request clientAuth = authentication.getOAuth2Request().refresh(tokenRequest);
        if (Objects.nonNull(tokenRequestScope) && !tokenRequestScope.isEmpty()) {
            Set<String> originalScope = clientAuth.getScope();
            if (Objects.isNull(originalScope) || !originalScope.containsAll(tokenRequestScope)) {
                throw new InvalidScopeException("Unable to narrow the scope of the client authentication to " + tokenRequestScope + ".", originalScope);
            } else {
                clientAuth = clientAuth.narrowScope(tokenRequestScope);
            }
        }
        return new OAuth2Authentication(clientAuth, authentication.getUserAuthentication());
    }

    // =================================================================================================================

    /**
     * Description: 令牌生成器
     *
     * @author LiKe
     * @date 2020-07-08 18:36:41
     */
    private final class TokenGenerator {

        /**
         * Description: 创建 refresh-token<br>
         * Details: 如果采用 JwtTokenStore, OAuth2RefreshToken 最终会在 JWtAccessTokenConverter 中被包装成用私钥加密后的以 OAuth2AccessToken 作为 payload 的 JWT 格式
         *
         * @param authentication {@link OAuth2Authentication}
         * @return org.springframework.security.oauth2.common.OAuth2RefreshToken
         * @author LiKe
         * @date 2020-07-09 15:52:28
         */
        public OAuth2RefreshToken createRefreshToken(OAuth2Authentication authentication) {
            if (!isSupportRefreshToken(authentication)) {
                return null;
            }

            final int validitySeconds = getRefreshTokenValiditySeconds(authentication.getOAuth2Request());
            final String tokenValue = UUID.randomUUID().toString();
            if (validitySeconds > 0) {
                return new DefaultExpiringOAuth2RefreshToken(tokenValue, new Date(System.currentTimeMillis() + validitySeconds * 1000L));
            }

            // 返回不过期的 refresh-token
            return new DefaultOAuth2RefreshToken(tokenValue);
        }

        /**
         * Description: 创建 access-token<br>
         * Details: 如果采用 JwtTokenStore, OAuth2AccessToken 最终会在 JWtAccessTokenConverter 中被包装成用私钥加密后的以 OAuth2AccessToken 作为 payload 的 JWT 格式
         *
         * @param authentication {@link OAuth2Authentication}
         * @param refreshToken   {@link OAuth2RefreshToken}
         * @return org.springframework.security.oauth2.common.OAuth2AccessToken
         * @author LiKe
         * @date 2020-07-09 15:51:29
         */
        public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
            DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
            int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
            if (validitySeconds > 0) {
                token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
            }
            token.setRefreshToken(refreshToken);
            token.setScope(authentication.getOAuth2Request().getScope());

            return accessTokenEnhancer != null ? accessTokenEnhancer.enhance(token, authentication) : token;
        }
    }

    @Override
    protected int getAccessTokenValiditySeconds(OAuth2Request clientAuth) {
        if (clientDetailsService != null) {
            ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
            Integer validity = client.getAccessTokenValiditySeconds();
            if (validity != null) {
                return validity;
            }
        }
        return accessTokenValiditySeconds;
    }

    /**
     * The refresh token validity period in seconds
     *
     * @param clientAuth the current authorization request
     * @return the refresh token validity period in seconds
     */
    @Override
    protected int getRefreshTokenValiditySeconds(OAuth2Request clientAuth) {
        if (clientDetailsService != null) {
            ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
            Integer validity = client.getRefreshTokenValiditySeconds();
            if (validity != null) {
                return validity;
            }
        }
        return refreshTokenValiditySeconds;
    }


    // =================================================================================================================

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}

