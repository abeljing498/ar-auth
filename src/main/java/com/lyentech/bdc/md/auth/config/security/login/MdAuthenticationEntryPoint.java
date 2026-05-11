package com.lyentech.bdc.md.auth.config.security.login;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.CharStreams;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import lombok.SneakyThrows;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * @author guolanren
 */
public class  MdAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public MdAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @SneakyThrows
    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
//        String loginUri = "http://md.leayun.cn/auth/login";
        String loginPath = "/login";
        String requestPath = request.getServletPath();

        // path: /oauth/authorize

        if ("/oauth/authorize".equals(requestPath)) {
//            String authorizeUri = "http://md.leayun.cn/auth/oauth/authorize";
            String authorizePath = "/oauth/authorize";
            String authorizeParams = request.getQueryString();

            authorizeParams = URLDecoder.decode(authorizeParams, "UTF-8");

            String returnTo = UriComponentsBuilder.fromPath(authorizePath)
                    .toUriString();

            if (authorizeParams != null) {
                returnTo = returnTo + "?" + authorizeParams;
            }


            // 构建带 return_to 参数 url
            String redirectUrlWithParams = UriComponentsBuilder
                    .fromPath(loginPath)
                    .queryParam("return_to", returnTo)
                    .build()
                    .encode()
                    .toUriString();
            return redirectUrlWithParams;

        }

        // path: /error
        if ("/error".equals(requestPath)) {
            try {
                String requestBody = CharStreams.toString(request.getReader());
                MdLoginParam loginParam = JSONObject.parseObject(requestBody, MdLoginParam.class);

                String returnTo;
                if (loginParam != null && (returnTo = loginParam.getReturnTo()) != null) {
                    // 构建带 return_to 参数 url
                    String redirectUrlWithParams = UriComponentsBuilder
                            .fromPath(loginPath)
                            .queryParam("return_to", URLDecoder.decode(returnTo, "utf-8"))
                            .build()
                            .encode()
                            .toUriString();
                    return redirectUrlWithParams;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return loginPath;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
        HttpResponseUtil.setResultEntityAsContent(response, ResultEntity.forward(redirectUrl));
    }
}
