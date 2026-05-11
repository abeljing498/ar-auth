package com.lyentech.bdc.md.auth.config.security.logout;

import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.config.feign.FeignSsoLoginService;
import com.lyentech.bdc.md.auth.dao.AppSsoMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginOutLogMapper;
import com.lyentech.bdc.md.auth.model.entity.AppSso;
import com.lyentech.bdc.md.auth.model.entity.MdLoginOutLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * @author guolanren
 */
public class OAuth2LogoutHandler implements LogoutHandler {

    private static Logger logger = LoggerFactory.getLogger(OAuth2LogoutHandler.class);
    private TokenStore tokenStore;
    private FeignSsoLoginService ssoLoginService;
    private AppSsoMapper appSsoMapper;
    private MdLoginOutLogMapper mdLoginOutLogMapper;

    public OAuth2LogoutHandler(TokenStore tokenStore, FeignSsoLoginService ssoLoginService, AppSsoMapper appSsoMapper, MdLoginOutLogMapper mdLoginOutLogMapper) {
        this.tokenStore = tokenStore;
        this.ssoLoginService = ssoLoginService;
        this.appSsoMapper = appSsoMapper;
        this.mdLoginOutLogMapper = mdLoginOutLogMapper;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = request.getParameter("refreshToken");
        OAuth2RefreshToken refreshTokenEntity = tokenStore.readRefreshToken(refreshToken);
        // 移除 accessToken, refreshToken
        tokenStore.removeAccessTokenUsingRefreshToken(refreshTokenEntity);
        tokenStore.removeRefreshToken(refreshTokenEntity);
        String ssoToken = request.getParameter("ssoToken");
        if (!StringUtils.isEmpty(request.getParameter("operateUserId"))) {
            MdLoginOutLog mdLoginOutLog = new MdLoginOutLog();
            mdLoginOutLog.setAppId(request.getParameter("appKey"));
            mdLoginOutLog.setCreateTime(new Date());
            if (!StringUtils.isEmpty(request.getParameter("operateUserId"))) {
                mdLoginOutLog.setUserId(Long.parseLong(request.getParameter("operateUserId")));
            }
            if (!StringUtils.isEmpty(request.getParameter("operateUserName"))) {
                mdLoginOutLog.setUserName(URLUtil.decode(request.getParameter("operateUserName"), "UTF-8"));
            }
            mdLoginOutLog.setIp(request.getParameter("userIp"));
            if (!StringUtils.isEmpty(request.getParameter("tenantId"))) {
                mdLoginOutLog.setTenantId(Long.parseLong(request.getParameter("tenantId")));
            }
            mdLoginOutLogMapper.insert(mdLoginOutLog);
        }
        if (!StringUtils.isEmpty(ssoToken)) {
            String appKey = request.getParameter("appKey");
            AppSso appSso = appSsoMapper.selectOne(Wrappers.<AppSso>lambdaQuery().eq(AppSso::getAppId, appKey));
            Map map = ssoLoginService.loginOut(ssoToken, appSso.getSsoAppId(), appSso.getSsoAppKey());
            logger.info(map.toString());
        }
    }
}
