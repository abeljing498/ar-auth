package com.lyentech.bdc.md.auth.config.feign;

import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author :yan
 * @Date :Create in 2022/7/1
 * @Description :
 */

@Configuration
public class FeignConfig {


    @Bean
    public FeignSsoLoginService feignLoginService() {
        String url = "http://wfserver.gree.com";
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        return Feign.builder()
                .decoder(new GsonDecoder(builder.create()))
                .encoder(new GsonEncoder())
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(FeignSsoLoginService.class, url);
    }

    /**
     * 关闭feign失败重试功能
     * @return
     */
    @Bean
    public Retryer closeFeignRetry(){
        return Retryer.NEVER_RETRY;
    }
}
