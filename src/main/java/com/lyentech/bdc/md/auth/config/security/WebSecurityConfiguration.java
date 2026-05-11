package com.lyentech.bdc.md.auth.config.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.kr.starter.service.KrInfoPush;
import com.lyentech.bdc.md.auth.config.feign.FeignSsoLoginService;
import com.lyentech.bdc.md.auth.config.security.asf.MdAfsCheckFilter;
import com.lyentech.bdc.md.auth.config.security.login.MdAuthenticationEntryPoint;
import com.lyentech.bdc.md.auth.config.security.login.customer.MdCustomerAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.customer.MdCustomerAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.customer.MdCustomerAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.customer.MdCustomerAuthenticationSuccessHandler;
import com.lyentech.bdc.md.auth.config.security.login.filter.MdLoginPreProcessFilter;
import com.lyentech.bdc.md.auth.config.security.login.handler.MdRedirectAuthenticationSuccessHandler;
import com.lyentech.bdc.md.auth.config.security.login.online.MdOnlineAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.online.MdOnlineAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.online.MdOnlineAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.online.MdOnlineAuthenticationSuccessHandler;
import com.lyentech.bdc.md.auth.config.security.login.outauth.MdOutAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.outauth.MdOutAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.outauth.MdOutAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.password.MdPasswordAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.password.MdPasswordAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.sign.MdSignAuthenticationFailureHandle;
import com.lyentech.bdc.md.auth.config.security.login.sign.MdSignAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.sign.MdSignAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.sms.MdSmsAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.sms.MdSmsAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.sms.MdSmsAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.smscus.MdSmsCusAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.smscus.MdSmsCusAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.sso.MdSsoAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.sso.MdSsoAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.sso.MdSsoAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.unicom.MdUnicomAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.login.unicom.MdUnicomAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.wechat.MdWeChatAuthenticationFailureHandler;
import com.lyentech.bdc.md.auth.config.security.login.wechat.MdWeChatAuthenticationProvider;
import com.lyentech.bdc.md.auth.config.security.login.wechat.MdWeChatAuthenticationProcessingFilter;
import com.lyentech.bdc.md.auth.config.security.logout.OAuth2LogoutHandler;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.service.MdUserChannelService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.tencent.service.AfsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;

/**
 * 配置安全访问方式
 *
 * @author guolanren
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@Profile({"local", "dev", "pre", "test", "prod"})
@Slf4j
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${lyentech.chemical.plant.url}")
    private String chemicalPlatUrl;
    @Value("${auth.auto.regist.app.id}")
    private String authAutoRegistAppId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${wx.url}")
    private String wxUrl;
    @Autowired
    private MdUserService userService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FeignSsoLoginService ssoLoginService;

    @Resource
    private AppSsoMapper appSsoMapper;

    @Resource
    private MdLoginOutLogMapper mdLoginOutLogMapper;

    @Resource
    private MdLoginLogMapper loginLogMapper;

    @Autowired
    private MdBlackUserMapper blackUserMapper;

    @Autowired
    private KrInfoPush krInfoPush;

    @Autowired
    private MdUserMapper userMapper;
    @Autowired
    OrgUserService orgUserService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private MdUserChannelService mdUserChannelService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AfsService afsService;

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        ProviderManager providerManager = (ProviderManager) super.authenticationManager();
        providerManager.getProviders().add(smsAuthenticationProvider());
        providerManager.getProviders().add(ssoAuthenticationProvider());
        providerManager.getProviders().add(wechatAuthenticationProvider());
        providerManager.getProviders().add(signAuthenticationProvider());
        providerManager.getProviders().add(outAuthenticationProvider());
        providerManager.getProviders().add(customerAuthenticationProvider());
        providerManager.getProviders().add(onlineAuthenticationProvider());
        providerManager.getProviders().add(smsCusAuthenticationProvider());
        providerManager.getProviders().add(unicomAuthenticationProvider());
        return providerManager;
    }

    @Bean
    public AuthenticationProvider smsAuthenticationProvider() {
        return new MdSmsAuthenticationProvider(userService, stringRedisTemplate, krInfoPush);
    }

    @Bean
    public AuthenticationProvider ssoAuthenticationProvider() {
        return new MdSsoAuthenticationProvider(userService, ssoLoginService, appSsoMapper, blackUserMapper, authAutoRegistAppId, log);
    }
    @Bean
    public AuthenticationProvider wechatAuthenticationProvider() {
        return new MdWeChatAuthenticationProvider(userService, blackUserMapper, log,wxUrl);
    }
    @Bean
    public AuthenticationProvider signAuthenticationProvider() {
        return new MdSignAuthenticationProvider(userMapper, blackUserMapper, stringRedisTemplate);
    }

    @Bean
    public AuthenticationProvider outAuthenticationProvider() {
        return new MdOutAuthenticationProvider(userService, orgUserService, restTemplate, blackUserMapper, authAutoRegistAppId, log, env, mdUserChannelService);
    }

    @Bean
    public AuthenticationProvider customerAuthenticationProvider() {
        return new MdCustomerAuthenticationProvider(userService, log, env, restTemplate);
    }

    @Bean
    public AuthenticationProvider smsCusAuthenticationProvider() {
        return new MdSmsCusAuthenticationProvider(log, env);
    }

    @Bean
    public AuthenticationProvider onlineAuthenticationProvider() {
        return new MdOnlineAuthenticationProvider(userService, log, env, restTemplate);
    }

    @Bean
    public AuthenticationProvider unicomAuthenticationProvider() {
        return new MdUnicomAuthenticationProvider(log, env);
    }


    /**
     * 配置用户账户认证方式
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
//        用来配置忽略掉的 URL 地址，一般静态文件
        web.ignoring()
                .antMatchers("/css/**", "/js/**", "/favicon.ico", "/assets/**", "/ws/**");
    }

    /**
     * 配置路径访问拦截策略
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/login/**").permitAll() // 路径匿名访问，用于登录
                .antMatchers(HttpMethod.POST, "/verification_code").permitAll() // 路径匿名登录，用于发送验证
                .antMatchers(HttpMethod.POST, "/customer/login").permitAll()
                .antMatchers(HttpMethod.POST, "/online/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .and()
                .logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .addLogoutHandler(new OAuth2LogoutHandler(tokenStore, ssoLoginService, appSsoMapper, mdLoginOutLogMapper))
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .and()
                .exceptionHandling()
                .defaultAuthenticationEntryPointFor(authenticationEntryPoint(), new AntPathRequestMatcher("/**"))
                .and()
                // 添加客服系统验证处理过滤器
                .addFilterBefore(customerAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                //添加在线认证过滤器
                .addFilterBefore(onlineAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                // 添加密码验证处理过滤器
                .addFilterAt(passwordAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                // 添加短信验证处理过滤器
                .addFilterBefore(smsAuthenticationProcessingFilter(), MdSmsAuthenticationProcessingFilter.class)
                //添加单点登录验证处理过滤器
                .addFilterBefore(ssoAuthenticationProcessingFilter(), MdSsoAuthenticationProcessingFilter.class)
                //添加微信小程序登录
                .addFilterBefore(wechatAuthenticationProcessingFilter(), MdWeChatAuthenticationProcessingFilter.class)
                //添加签名验证处理过滤器
                .addFilterBefore(signAuthenticationProcessingFilter(), MdSignAuthenticationProcessingFilter.class)
                .addFilterBefore(outAuthenticationProcessingFilter(), MdOutAuthenticationProcessingFilter.class)
                .addFilterBefore(smsCusAuthenticationProcessingFilter(), MdSmsCusAuthenticationProcessingFilter.class)
                .addFilterBefore(unicomAuthenticationProcessingFilter(), MdUnicomAuthenticationProcessingFilter.class)
                // 添加登录预处理过滤器
                .addFilterAfter(new MdLoginPreProcessFilter(chemicalPlatUrl), LogoutFilter.class)
                .addFilterAfter(new MdAfsCheckFilter(afsService, "/verification_code"),
                        MdSmsAuthenticationProcessingFilter.class)
                .csrf()
                .ignoringAntMatchers("/login", "/verification_code", "/logout", "/customer/login", "/online/login")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .headers()
                .frameOptions().disable()
                .and()
                .cors();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(CorsConfiguration.ALL);
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private MdAuthenticationEntryPoint authenticationEntryPoint() {
        MdAuthenticationEntryPoint authenticationEntryPoint = new MdAuthenticationEntryPoint("/login");
        authenticationEntryPoint.setForceHttps(true);
        return authenticationEntryPoint;
    }

    /**
     * 密码登录的认证处理过滤器
     *
     * @return
     * @throws Exception
     */
    private MdPasswordAuthenticationProcessingFilter passwordAuthenticationProcessingFilter() throws Exception {
        MdPasswordAuthenticationProcessingFilter passwordAuthenticationProcessingFilter = new MdPasswordAuthenticationProcessingFilter(afsService);
        passwordAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        passwordAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        passwordAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdPasswordAuthenticationFailureHandler());
        return passwordAuthenticationProcessingFilter;
    }

    /**
     * 短信登录的认证处理过滤器
     *
     * @return
     * @throws Exception
     */
    private MdSmsAuthenticationProcessingFilter smsAuthenticationProcessingFilter() throws Exception {
        MdSmsAuthenticationProcessingFilter smsAuthenticationProcessingFilter = new MdSmsAuthenticationProcessingFilter();
        smsAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        smsAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        smsAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdSmsAuthenticationFailureHandler());
        return smsAuthenticationProcessingFilter;
    }

    /**
     * 单点登录的认证处理过滤器
     *
     * @return
     * @throws Exception
     */
    private MdSsoAuthenticationProcessingFilter ssoAuthenticationProcessingFilter() throws Exception {
        MdSsoAuthenticationProcessingFilter ssoAuthenticationProcessingFilter = new MdSsoAuthenticationProcessingFilter();
        ssoAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        ssoAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        ssoAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdSsoAuthenticationFailureHandler());
        return ssoAuthenticationProcessingFilter;
    }

    private MdWeChatAuthenticationProcessingFilter wechatAuthenticationProcessingFilter() throws Exception {
        MdWeChatAuthenticationProcessingFilter weChatAuthenticationProcessingFilter = new MdWeChatAuthenticationProcessingFilter();
        weChatAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        weChatAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        weChatAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdWeChatAuthenticationFailureHandler());
        return weChatAuthenticationProcessingFilter;
    }

    /**
     * 签名登录的认证处理过滤器
     *
     * @return
     * @throws Exception
     */
    private MdSignAuthenticationProcessingFilter signAuthenticationProcessingFilter() throws Exception {
        MdSignAuthenticationProcessingFilter signAuthenticationProcessingFilter = new MdSignAuthenticationProcessingFilter();
        signAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        signAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        signAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdSignAuthenticationFailureHandle());
        return signAuthenticationProcessingFilter;
    }

    private MdOutAuthenticationProcessingFilter outAuthenticationProcessingFilter() throws Exception {
        MdOutAuthenticationProcessingFilter outAuthenticationProcessingFilter = new MdOutAuthenticationProcessingFilter();
        outAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        outAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        outAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdOutAuthenticationFailureHandler());
        return outAuthenticationProcessingFilter;
    }

    private MdSmsCusAuthenticationProcessingFilter smsCusAuthenticationProcessingFilter() throws Exception {
        MdSmsCusAuthenticationProcessingFilter outAuthenticationProcessingFilter = new MdSmsCusAuthenticationProcessingFilter();
        outAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        outAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        outAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdOutAuthenticationFailureHandler());
        return outAuthenticationProcessingFilter;
    }

    private MdOnlineAuthenticationProcessingFilter onlineAuthenticationProcessingFilter() throws Exception {
        MdOnlineAuthenticationProcessingFilter customerAuthenticationProcessingFilter = new MdOnlineAuthenticationProcessingFilter(objectMapper);
        customerAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        customerAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdOnlineAuthenticationSuccessHandler(objectMapper, loginLogMapper, tokenStore));
        customerAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdOnlineAuthenticationFailureHandler(objectMapper));
        return customerAuthenticationProcessingFilter;
    }

    private MdUnicomAuthenticationProcessingFilter unicomAuthenticationProcessingFilter() throws Exception {
        MdUnicomAuthenticationProcessingFilter customerAuthenticationProcessingFilter = new MdUnicomAuthenticationProcessingFilter();
        customerAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        customerAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdRedirectAuthenticationSuccessHandler(loginLogMapper));
        customerAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdOutAuthenticationFailureHandler());
        return customerAuthenticationProcessingFilter;
    }

    private MdCustomerAuthenticationProcessingFilter customerAuthenticationProcessingFilter() throws Exception {
        MdCustomerAuthenticationProcessingFilter customerAuthenticationProcessingFilter = new MdCustomerAuthenticationProcessingFilter(objectMapper);
        customerAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        customerAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new MdCustomerAuthenticationSuccessHandler(objectMapper, loginLogMapper, tokenStore));
        customerAuthenticationProcessingFilter.setAuthenticationFailureHandler(new MdCustomerAuthenticationFailureHandler(objectMapper));
        return customerAuthenticationProcessingFilter;
    }

}
