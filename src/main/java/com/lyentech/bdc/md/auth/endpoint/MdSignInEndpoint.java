package com.lyentech.bdc.md.auth.endpoint;

import cn.hutool.core.util.StrUtil;
import com.lyentech.bdc.md.auth.config.security.login.MdLoginProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * @author guolanren
 */
@Controller
@Slf4j
public class MdSignInEndpoint {

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private MdLoginProperties mdLoginProperties;

    /**
     * 为了manager模块前后的联调方便，如果是dev环境直接重定项到auth模块的内部静态页面index（此情况下前端不需要启动auth服务的前端页面），
     * 如前后端需联调auth模块页面请注释掉if语句，并配置正确的重定向页面
     */
    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        log.info("request类型:{}", request.getScheme());
        if ("dev".equals(env)) {
            return "index";
        }
        String loginPageUrl = mdLoginProperties.getLoginPageUrl();
        String returnTo = request.getParameter("return_to");
        String inner = StrUtil.subAfter(returnTo, "inner=", true);
        log.info(inner);
        if (StrUtil.isNotBlank(inner)) {
            if (!Boolean.valueOf(inner)) {
                loginPageUrl = new StringBuilder()
                        .append("https")
                        .append(":")
                        .append(StrUtil.subAfter(loginPageUrl, ":", false)).toString();
                log.info("修改后的请求地址:{}", loginPageUrl);
            }
        }
        String redirectUrlWithParams = UriComponentsBuilder
                .fromHttpUrl(loginPageUrl)
                .queryParam("return_to", returnTo)
                .build()
                .encode()
                .toUriString();
        return "redirect:" + redirectUrlWithParams;
//        return "index";
    }

    @PostMapping(value = "/customer/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public void customerLogin(@RequestBody Map<String, String> loginRequest) {
        // 这个方法不需要实现任何逻辑
        // 因为认证逻辑已经在 MdCustomerAuthenticationProcessingFilter 中处理
        // 如果请求到达这里，说明认证已经成功
    }
    @PostMapping(value = "/online/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public void onlineLogin(@RequestBody Map<String, String> loginRequest) {
        // 这个方法不需要实现任何逻辑
        // 因为认证逻辑已经在 MdCustomerAuthenticationProcessingFilter 中处理
        // 如果请求到达这里，说明认证已经成功
    }
}
