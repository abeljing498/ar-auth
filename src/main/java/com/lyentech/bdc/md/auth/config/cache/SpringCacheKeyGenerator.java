package com.lyentech.bdc.md.auth.config.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

@Component
public class SpringCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {

        char sp = ':';
        StringBuilder strBuilder = new StringBuilder(30);
        // 类名
        strBuilder.append(target.getClass().getSimpleName());
        strBuilder.append(sp);
        // 方法名
        strBuilder.append(method.getName());
        if (params.length > 0) {
            // 参数值
            for (Object object : params) {
                strBuilder.append("-");
                if (ObjectUtils.isEmpty(object)) {
                    strBuilder.append("null");
                    continue;
                }
                if (BeanHelper.isSimpleValueType(object.getClass())) {
                    strBuilder.append(object);
                } else {
                    String jsonString = JSON.toJSONString(object, SerializerFeature.WriteMapNullValue);
                    strBuilder.append(jsonString.hashCode());
                }
            }
        } else {
            strBuilder.append(0);
        }
        return strBuilder.toString();
    }

}
