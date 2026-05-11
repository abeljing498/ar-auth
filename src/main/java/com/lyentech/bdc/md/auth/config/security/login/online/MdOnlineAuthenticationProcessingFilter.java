package com.lyentech.bdc.md.auth.config.security.login.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MdOnlineAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;

    public MdOnlineAuthenticationProcessingFilter(ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher("/online/login", "POST"));
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        try {
            // 读取请求体
            String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            // 打印请求体以便调试
            logger.info("在线组件鉴权传入参数: " + requestBody);
            // 解析JSON
            Map<String, String> authRequest = objectMapper.readValue(requestBody, Map.class);
            String loginSystem="";
            String loginBrowser="";
            String userIp="";
            String appId="";
            if (authRequest.get("authCode") == null) {
                throw new AuthenticationServiceException("认证code不能为空！");
            }
            if (authRequest.get("appId")==null) {
                throw new AuthenticationServiceException("appId不能为空！");
            }
            if (authRequest.get("tenantId")==null) {
                throw new AuthenticationServiceException("租户ID不能为空！");
            }
            if (authRequest.get("userIp") != null) {
                userIp=authRequest.get("userIp");
            }
            String authCode = authRequest.get("authCode");
            Long tanantId = Long.parseLong(authRequest.get("tenantId"));
             appId = authRequest.get("appId");
            loginSystem = authRequest.get("loginSystem");
            loginBrowser = authRequest.get("loginBrowser");
            MdOnlineAuthenticationToken authRequestToken = new MdOnlineAuthenticationToken(authCode,appId,loginSystem,loginBrowser,userIp,tanantId);
            return this.getAuthenticationManager().authenticate(authRequestToken);
        } catch (IOException e) {
            logger.error("Failed to parse authentication request", e);
            throw new AuthenticationServiceException("Failed to parse authentication request: " + e.getMessage());
        }
    }

}