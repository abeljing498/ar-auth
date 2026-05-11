package com.lyentech.bdc.md.auth.config.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * @Author :yan
 * @Date :Create in 2021/6/26
 * @Description :
 */

@Configuration
public class RedisConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Primary
    @Bean(name = "redisConnectionFactory")
    public LettuceConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig());
    }

    private LettuceClientConfiguration clientConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        if (pool != null) {
            poolConfig.setMaxTotal(pool.getMaxActive());
            poolConfig.setMaxIdle(pool.getMaxIdle());
            poolConfig.setMinIdle(pool.getMinIdle());
            poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }

        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(5000))
                .poolConfig(poolConfig)
                .build();
        return clientConfig;
    }

}
