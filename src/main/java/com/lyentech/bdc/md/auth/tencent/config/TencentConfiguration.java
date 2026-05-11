package com.lyentech.bdc.md.auth.tencent.config;

import com.lyentech.bdc.md.auth.tencent.properties.AfsProperties;
import com.lyentech.bdc.md.auth.tencent.properties.SmsProperties;
import com.lyentech.bdc.md.auth.tencent.service.AfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author guolanren
 */
@Configuration
@EnableConfigurationProperties({AfsProperties.class, SmsProperties.class})
public class TencentConfiguration {

//    @Autowired
//    private SmsProperties smsProperties;
    @Autowired
    private AfsProperties afsProperties;

    /**
     * 登录短信服务
     */
//    @Bean
//    public SmsService loginSmsService() throws Exception {
//        SmsService smsService = new SmsService(smsProperties.getSecretId(), smsProperties.getSecretKey());
//        smsService.setSmsSdkAppId(smsProperties.getSmsSdkAppId());
//        smsService.setTemplateId(smsProperties.getTemplateId());
//        smsService.setSign(smsProperties.getSign());
//        return smsService;
//    }

    /**
     * afs 人机验证服务
     */
    @Bean
    public AfsService afsService() throws Exception {
        return new AfsService(afsProperties);
    }
}
