package com.lyentech.bdc.md.auth.config.feign;

import feign.Param;
import feign.RequestLine;

import java.util.Map;

/**
 * @author yan
 */
public interface FeignSsoLoginService {

    @RequestLine("GET /sso/ssoapi/GetToken?callback={callback}&appid={appId}&appkey={appKey}")
    Map getToken(@Param("callback") String callback,
                 @Param("appId") String appId,
                 @Param("appKey") String appKey);

    @RequestLine("GET /sso/ssoapi/GetUserInfo?token={token}&appid={appId}&appkey={appKey}&ip={ip}")
    Map getUser(@Param("token") String token,
                @Param("appId") String appId,
                @Param("appKey") String appKey,
                @Param("ip") String ip);

    @RequestLine("GET /sso/ssoapi/SignOut?token={token}&appid={appId}&appkey={appKey}")
    Map loginOut(@Param("token") String token,
                 @Param("appId") String appId,
                 @Param("appKey") String appKey);
}
