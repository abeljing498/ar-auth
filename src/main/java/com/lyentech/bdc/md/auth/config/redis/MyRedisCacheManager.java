package com.lyentech.bdc.md.auth.config.redis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @Author :yan
 * @Date :Create in 2022/8/16
 * @Description :
 */

public class MyRedisCacheManager extends RedisCacheManager {

    public MyRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];
        if (array.length > 1) {
            // 解析TTL
            long ttl = Long.parseLong(array[1]);
            // 注意单位此处用的是秒，而非毫秒
            cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(ttl));
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
