package com.lyentech.bdc.md.auth.config.security.login;

import com.lyentech.bdc.exception.IllegalParamException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author :yan
 * @Date :Create in 2022/7/26
 * @Description :
 */

@Component
@ConfigurationProperties(prefix = "auth")
public class MdLoginProperties {

    /**
     * 登录页url
     */
    @Value("${auth.login.page.url}")
    private String loginPageUrl;

    public String getLoginPageUrl() {
        if (loginPageUrl == null) {
            throw new IllegalParamException("配置文件属性 [auth.login.page.url] 未配置");
        }
        return loginPageUrl;
    }

    public void setLoginPageUrl(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
    }
}
