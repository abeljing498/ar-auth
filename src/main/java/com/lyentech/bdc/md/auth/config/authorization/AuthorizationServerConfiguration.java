package com.lyentech.bdc.md.auth.config.authorization;

import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * 配置授权服务器
 *
 * @author guolanren
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private MdAppService appService;

    // token 存储方式配置
    @Autowired
    private RedisTokenStore tokenStore;

    @Autowired
    private AuthenticationManager authenticationManager;

    // code 存储方式配置
    @Autowired
    private AuthorizationCodeServices authorizationCodeServices;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;

    /**
     * 配置授权服务器的安全校验
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // app secret 不加密
        security.passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    /**
     * 配置使用哪种方式来维护 app 信息
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 自定义 ClientDetails
        clients.withClientDetails(appService);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                // redis 存储 token(配置 token 存放方式)
                .tokenStore(tokenStore)
                // 支持 password 授权模式
                .authenticationManager(authenticationManager)
                // redis 存储授权码
                // code 的生成方式是配置一个配置，在里面配置 code 的生成配置
                .authorizationCodeServices(authorizationCodeServices)
                .tokenGranter(new CustomTokenGranter(endpoints, authenticationManager))
                .tokenEnhancer(endpoints.getTokenEnhancer())
                .tokenServices(new CustomAuthorizationServerTokenServices(endpoints))

                // 拦截器，删除认证会话
                .addInterceptor(new AuthSessionHandlerInterceptor());
    }
}
