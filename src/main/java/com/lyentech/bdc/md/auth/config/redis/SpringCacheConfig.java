package com.lyentech.bdc.md.auth.config.redis;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * @Author :yan
 * @Date :Create in 2022/8/16
 * @Description : 自定义spring cache序列化方式
 */

@Configuration
@EnableCaching
public class SpringCacheConfig extends CachingConfigurerSupport {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        //配置缓存的序列化方式和默认失效时间（1天）
        RedisSerializer serializer = new SpringCacheFastJsonRedisSerializer(Object.class);
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .computePrefixWith(cacheName -> "caching:" + cacheName);

        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        MyRedisCacheManager redisCacheManager = new MyRedisCacheManager(redisCacheWriter, defaultCacheConfig);
        return redisCacheManager;
    }

}
