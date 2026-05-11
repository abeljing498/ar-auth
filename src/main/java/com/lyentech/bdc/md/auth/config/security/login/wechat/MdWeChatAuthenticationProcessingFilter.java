package com.lyentech.bdc.md.auth.config.security.login.wechat;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSsoErrorException;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.WECHAT;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.MD_LOGIN_BODY;

/**
 * @author guolanren
 */
public class MdWeChatAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {

    private boolean postOnly = true;
    private boolean continueChainBeforeSuccessfulAuthentication = false;
    private SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy();

    public MdWeChatAuthenticationProcessingFilter() {
        super();
    }

    @Override
    public void setFilterProcessesUrl(String filterProcessesUrl) {
        super.setFilterProcessesUrl(filterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        MdLoginParam loginParam = obtainLoginParam(request);
        logger.info("用户通过微信认证参数为："+JSON.toJSONString(loginParam));
        // 单点登录
        String authType = loginParam.getAuthType();
        if (WECHAT.equals(authType)) {
            String wechatCode = loginParam.getWechatCode();
            if (wechatCode == null) {
                wechatCode = "";
            }

            String returnTo = null;
            try {
                returnTo = URLDecoder.decode(loginParam.getReturnTo(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MdLoginSsoErrorException("回调地址解码失败");
            }
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(returnTo).build();
            List<String> clients = uriComponents.getQueryParams().get("client_id");
            String clientId = clients.get(0);
            String userIp= ServletUtil.getClientIP(request);
            MdWeChatAuthenticationToken authRequest = new MdWeChatAuthenticationToken(clientId, wechatCode,loginParam.getLoginSystem(),loginParam.getLoginBrowser(),userIp);
            return this.getAuthenticationManager().authenticate(authRequest);
        } else {
            return null;
        }
    }

    protected MdLoginParam obtainLoginParam(HttpServletRequest request) {
        MdLoginParam loginParam = (MdLoginParam) request.getAttribute(MD_LOGIN_BODY);
        return loginParam;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Request is to process authentication");
        }

        Authentication authResult;

        try {
            authResult = attemptAuthentication(request, response);
            if (authResult == null) {
                chain.doFilter(request, response);
                return;
            }
            sessionStrategy.onAuthentication(authResult, request, response);
        } catch (InternalAuthenticationServiceException failed) {
            logger.error(
                    "An internal error occurred while trying to authenticate the user.",
                    failed);
            unsuccessfulAuthentication(request, response, failed);

            return;
        } catch (AuthenticationException failed) {
            // Authentication failed
            unsuccessfulAuthentication(request, response, failed);

            return;
        }

        // Authentication success
        if (continueChainBeforeSuccessfulAuthentication) {
            chain.doFilter(request, response);
        }

        successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    @Override
    public void setContinueChainBeforeSuccessfulAuthentication(boolean continueChainBeforeSuccessfulAuthentication) {
        this.continueChainBeforeSuccessfulAuthentication = continueChainBeforeSuccessfulAuthentication;
    }

}
