package com.lyentech.bdc.md.auth.tencent.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author guolanren
 */
@Component
@ConfigurationProperties(prefix = "tencent.afslogin")
@PropertySource(value = "classpath:config/tencent.yml", factory = YamlPropertyLoaderFactory.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAfsProperties {
    private String secretId;
    private String secretKey;
    private String action;
    private String version;
    private Long captchaType;
    private String userIp;
    private String appSecretKey;
    private Long captchaAppId;
}
