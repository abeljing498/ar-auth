package com.lyentech.bdc.md.auth.tencent.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author guolanren
 */
@Component
@ConfigurationProperties(prefix = "tencent.sms")
@PropertySource(value = "classpath:config/tencent.yml", factory = YamlPropertyLoaderFactory.class)
@Data
public class SmsProperties {

    private String secretId;
    private String secretKey;
    private String templateId;
    private String smsSdkAppId;
    private String sign;
    private String endpoint;

}
