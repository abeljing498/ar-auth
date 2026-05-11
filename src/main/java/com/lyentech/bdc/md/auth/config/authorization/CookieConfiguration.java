//package com.lyentech.bdc.md.auth.config.authorization;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.session.web.http.CookieSerializer;
//import org.springframework.session.web.http.DefaultCookieSerializer;
//
//@Configuration
//public class CookieConfiguration {
//    @Bean
//    public CookieSerializer httpSessionIdResolver(){
//        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
//        cookieSerializer.setCookieName("SESSION");
//        cookieSerializer.setUseHttpOnlyCookie(false);
//        cookieSerializer.setSameSite("None");
//        cookieSerializer.setUseSecureCookie(true);
//        return cookieSerializer;
//    }
//}
