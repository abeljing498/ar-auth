package com.lyentech.bdc.md.auth.config.resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

/**
 * 用户资源服务器
 *
 * @author guolanren
 */
@Configuration
@EnableResourceServer
public class ProfileResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    public static final String RESOURCE_ID = "profile";

    /**
     * 声明资源服务器id
     *
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(RESOURCE_ID);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/me", "/userInfo", "/account/**", "/user/**", "/roles", "/roleInfo", "/userTenant/**", "/org/**", "/role/**", "/orgUser/**", "/userTenantRole/**","/tenant/**","/log/**","/black/**","/userChannel/**") // 配置要拦截哪些资源
                .and()
                .authorizeRequests()  // 设置不同的url要用什么样的拦截策略
                .antMatchers("/me", "/userInfo", "/account/update", "/roles", "/userTenant/getList", "/org/add", "/org/update", "/role/add", "/role/updateRole", "/orgUser/add", "/orgUser/update","/tenant/add", "/tenant/update", "/black/update").authenticated()
                .antMatchers("/account/**", "user/**", "/org/**", "/role/**", "/orgUser/**","/tenant/**","/log/**","/black/**").permitAll();

    }
}
