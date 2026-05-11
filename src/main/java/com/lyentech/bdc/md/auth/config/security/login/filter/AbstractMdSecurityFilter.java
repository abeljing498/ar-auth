package com.lyentech.bdc.md.auth.config.security.login.filter;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guolanren
 */
abstract public class AbstractMdSecurityFilter extends GenericFilterBean {

    private List<RequestMatcher> requiresAuthenticationRequestMatchers;

    public AbstractMdSecurityFilter(String... antPatterns) {
        initRequestMatchers(antPatterns);
    }

    public AbstractMdSecurityFilter(HttpMethod method, String... antPatterns) {
        initRequestMatchers(method.name(), antPatterns);
    }

    protected boolean requiresAuthentication(HttpServletRequest request) {
        for (RequestMatcher requestMatcher : requiresAuthenticationRequestMatchers) {
            if (requestMatcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private void initRequestMatchers(String... antPatterns) {
        if (requiresAuthenticationRequestMatchers == null) {
            requiresAuthenticationRequestMatchers = new ArrayList<>();
        }

        for (String antPattern : antPatterns) {
            RequestMatcher requestMatcher = new AntPathRequestMatcher(antPattern);
            requiresAuthenticationRequestMatchers.add(requestMatcher);
        }
    }

    private void initRequestMatchers(String method, String... antPatterns) {
        if (requiresAuthenticationRequestMatchers == null) {
            requiresAuthenticationRequestMatchers = new ArrayList<>();
        }

        for (String antPattern : antPatterns) {
            RequestMatcher requestMatcher = new AntPathRequestMatcher(antPattern, method);
            requiresAuthenticationRequestMatchers.add(requestMatcher);
        }
    }
}
