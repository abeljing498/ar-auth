package com.lyentech.bdc.md.auth.config.authorization;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;

import java.util.concurrent.TimeUnit;

/**
 * 授权码存储的 Redis 方案，auth 服务多实例的统一外部储存
 *
 * @author guolanren
 */
@Configuration
public class AuthorizationCodeConfiguration {

    private static final String MD_AUTH_AUTHORIZATION_CODE_REDIS_KEY_PREFIX = "md:auth:authorization:code_";

    /**
     * 授权码生成器，默认 6 位
     */
    private RandomValueStringGenerator generator = new RandomValueStringGenerator();

    @Bean
    public AuthorizationCodeServices authorizationCodeServices(@Qualifier("authorizationCodeRedisTemplate") RedisTemplate redisTemplate) {
        return new RedisAuthorizationCodeServices(redisTemplate);
    }

    /**
     * 授权码专用的 RedisTemplate
     * K: String 类型
     * V: OAuth2Authentication 类型
     *
     * @param redisConnectionFactory redis 连接工厂
     * @return
     */
    @Bean("authorizationCodeRedisTemplate")
    public RedisTemplate<String, OAuth2Authentication> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, OAuth2Authentication> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.java());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


    private class RedisAuthorizationCodeServices implements AuthorizationCodeServices {

        private final RedisTemplate<String, OAuth2Authentication> redisTemplate;

        public RedisAuthorizationCodeServices(RedisTemplate<String, OAuth2Authentication> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        /**
         * 创建授权码
         *
         * @param authentication 用户认证完批准获取指定信息后的对象
         * @return
         */
        @Override
        public String createAuthorizationCode(OAuth2Authentication authentication) {
            String code = generator.generate();
            // 授权码 1 分钟内有效
            // 需要绑定好 code ： 用户+app 的映射关系，来作为生成token的凭证
            redisTemplate.boundValueOps(MD_AUTH_AUTHORIZATION_CODE_REDIS_KEY_PREFIX + code)
                    .set(authentication, 1, TimeUnit.MINUTES);
            return code;
        }

        /**
         * 消费授权码
         *
         * @param code
         * @return
         * @throws InvalidGrantException
         */
        @Override
        public OAuth2Authentication consumeAuthorizationCode(String code) throws InvalidGrantException {
            String codeKey = MD_AUTH_AUTHORIZATION_CODE_REDIS_KEY_PREFIX + code;
            // 授权码对应的 OAuth2Authentication
            OAuth2Authentication authentication = redisTemplate.boundValueOps(codeKey).get();
            // 授权码 1 次有效，用完即删，无论获取 token 成功失败
            redisTemplate.delete(codeKey);
            return authentication;
        }
    }
}
