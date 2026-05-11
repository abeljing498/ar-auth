package com.lyentech.bdc.md.auth.config.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * token 存储的 Redis 方案，auth 服务多实例的统一外部储存
 *
 * @author guolanren
 */
@Configuration
public class TokenConfiguration {

    private static final String MD_AUTH_AUTHORIZATION_TOKEN_REDIS_KEY_PREFIX = "md:auth:authorization:token:";

    @Bean
    public RedisTokenStore tokenService(RedisConnectionFactory redisConnectionFactory) {
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        // 设置 token 的 redis key 前缀
        redisTokenStore.setPrefix(MD_AUTH_AUTHORIZATION_TOKEN_REDIS_KEY_PREFIX);
        return redisTokenStore;
    }


}
