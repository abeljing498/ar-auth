package com.lyentech.bdc.md.auth.config.web;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.BeforeFilter;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guolanren
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {


    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");


        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
    }

    /**
     * 使用fastjson替换默认jackson
     *
     * @param converters
     */
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        // 自定义配置...
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializeFilters(new OAuthTokenSerializeFilter());
        fastJsonConfig.setFeatures(Feature.OrderedField);
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.IgnoreNonFieldGetter,
                SerializerFeature.WriteMapNullValue);
        fastConverter.setFastJsonConfig(fastJsonConfig);

        // 高版本fastjson默认'*/*'
        // java.lang.IllegalArgumentException: 'Content-Type' cannot contain wildcard type '*'
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        converters.add(0, fastConverter);
    }

    class OAuthTokenSerializeFilter extends BeforeFilter implements PropertyPreFilter {
        @Override
        public void writeBefore(Object object) {
            if (object instanceof DefaultOAuth2AccessToken) {
                DefaultOAuth2AccessToken accessToken = (DefaultOAuth2AccessToken) object;
                if (accessToken.getValue() != null) {
                    writeKeyValue("access_token", accessToken.getValue());
                }
                if (accessToken.getTokenType() != null) {
                    writeKeyValue("token_type", accessToken.getTokenType());
                }
                if (accessToken.getRefreshToken() != null && accessToken.getRefreshToken().getValue() != null) {
                    writeKeyValue("refresh_token", accessToken.getRefreshToken().getValue());
                }
                writeKeyValue("expires_in", accessToken.getExpiresIn());
                if (accessToken.getScope() != null) {
                    writeKeyValue("scope", String.join(" ", accessToken.getScope()));
                }
            }
        }

        @Override
        public boolean apply(JSONSerializer jsonSerializer, Object o, String s) {
            return !(o instanceof DefaultOAuth2AccessToken);
        }
    }
}
