package com.lyentech.bdc.md.auth.config.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * @author guolanren
 */
@Configuration
@EnableCaching
public class SpringCacheConfiguration {

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
