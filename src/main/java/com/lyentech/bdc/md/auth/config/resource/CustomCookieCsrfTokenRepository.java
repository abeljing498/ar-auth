//package com.lyentech.bdc.md.auth.config.resource;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseCookie;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//import org.springframework.security.web.csrf.CsrfToken;
//import org.springframework.security.web.csrf.CsrfTokenRepository;
//import org.springframework.security.web.csrf.DefaultCsrfToken;
//import org.springframework.util.Assert;
//import org.springframework.util.StringUtils;
//import org.springframework.web.util.WebUtils;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.UUID;
//
///**
// * @author 260583
// */
//public class CustomCookieCsrfTokenRepository  implements CsrfTokenRepository {
//
//    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
//    static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
//    static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";
//    private String parameterName = "_csrf";
//    private String headerName = "X-XSRF-TOKEN";
//    private String cookieName = "XSRF-TOKEN";
//    private boolean cookieHttpOnly = true;
//    private String cookiePath;
//    private String cookieDomain;
//
//    public void CookieCsrfTokenRepository() {
//    }
//
//
//
//    @Override
//    public CsrfToken generateToken(HttpServletRequest httpServletRequest) {
//        return new DefaultCsrfToken(this.headerName, this.parameterName, this.createNewToken());
//    }
//
//    @Override
//    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
//        String tokenValue = token == null ? "" : token.getToken();
//        Cookie cookie = new Cookie(this.cookieName, tokenValue);
//        cookie.setSecure(true);
//        ResponseCookie cookie1 = ResponseCookie.from(this.cookieName, tokenValue).path("/").sameSite("none").secure(true).build();
//        response.setHeader(HttpHeaders.SET_COOKIE,cookie1.toString());
//        response.addCookie(cookie);
//    }
//
//    @Override
//    public CsrfToken loadToken(HttpServletRequest request) {
//        Cookie cookie = WebUtils.getCookie(request, this.cookieName);
//        if (cookie == null) {
//            return null;
//        } else {
//            String token = cookie.getValue();
//            return !StringUtils.hasLength(token) ? null : new DefaultCsrfToken(this.headerName, this.parameterName, token);
//        }
//    }
//
//
//    public void setParameterName(String parameterName) {
//        Assert.notNull(parameterName, "parameterName is not null");
//        this.parameterName = parameterName;
//    }
//
//    public void setHeaderName(String headerName) {
//        Assert.notNull(headerName, "headerName is not null");
//        this.headerName = headerName;
//    }
//
//    public void setCookieName(String cookieName) {
//        Assert.notNull(cookieName, "cookieName is not null");
//        this.cookieName = cookieName;
//    }
//
//    public void setCookieHttpOnly(boolean cookieHttpOnly) {
//        this.cookieHttpOnly = cookieHttpOnly;
//    }
//
//    private String getRequestContext(HttpServletRequest request) {
//        String contextPath = request.getContextPath();
//        return contextPath.length() > 0 ? contextPath : "/";
//    }
//
//    public static CookieCsrfTokenRepository withHttpOnlyFalse() {
//        CookieCsrfTokenRepository result = new CookieCsrfTokenRepository();
//        result.setCookieHttpOnly(false);
//        return result;
//    }
//
//    private String createNewToken() {
//        return UUID.randomUUID().toString();
//    }
//
//    public void setCookiePath(String path) {
//        this.cookiePath = path;
//    }
//
//    public String getCookiePath() {
//        return this.cookiePath;
//    }
//
//    public void setCookieDomain(String cookieDomain) {
//        this.cookieDomain = cookieDomain;
//    }
//}
//
