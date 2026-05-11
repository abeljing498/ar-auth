package com.lyentech.bdc.md.auth.config.security.login.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MdCustomerAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final MdLoginLogMapper loginLogMapper;
    private final TokenStore tokenStore;

    public MdCustomerAuthenticationSuccessHandler(ObjectMapper objectMapper, MdLoginLogMapper loginLogMapper, TokenStore tokenStore) {
        this.objectMapper = objectMapper;
        this.loginLogMapper = loginLogMapper;
        this.tokenStore = tokenStore;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        ResultEntity resultEntity;
        // 读取请求体
        String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        // 获取用户手机号
        String phone = authentication.getName();

        Map<String, Object> responseBody = new HashMap<>();
        resultEntity = ResultEntity.success(authentication);
        objectMapper.writeValue(response.getWriter(), resultEntity);
    }
}