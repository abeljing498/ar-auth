package com.lyentech.bdc.md.auth.config.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author :yan
 * @Date :Create in 2022/8/16
 * @Description : 自定义序列化器，实现在 SpringDataRedis 中自定义的序列化
 */

public class SpringCacheFastJsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private Class<T> clazz;

    public SpringCacheFastJsonRedisSerializer(Class<T> clazz) {
        super();
        ParserConfig.getGlobalInstance().addAccept("com.lyentech.bdc.");
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return JSON.parseObject(str, clazz);
    }
}