package com.lyentech.bdc.md.auth.config.resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * app 资源服务器
 *
 * @author guolanren
 */
@Configuration
@EnableResourceServer
public class AppResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/app/**", "/external/userOrg/**", "/open/**", "/role/authFromManager")
                .and()
                .authorizeRequests()
                .antMatchers("/app/**", "/external/userOrg/**", "/open/**", "/role/authFromManager").permitAll();
    }
}