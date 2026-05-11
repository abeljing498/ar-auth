package com.lyentech.bdc.md.auth.config.authorization;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author guolanren
 */
public class AuthSessionHandlerInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HttpSession session = request.getSession();
        if (session != null) {
            session.setMaxInactiveInterval(1);
            session.invalidate();
        }
    }
}
