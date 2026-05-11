package com.lyentech.bdc.md.auth.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信AccessToken获取工具类
 */
@Slf4j
@Component
public class WeChatAccessTokenService {

    @Autowired
    private RestTemplate restTemplate;


    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;
    @Value("${wx.url}")
    private String wxUrl;
    private static final String CACHE_KEY_PREFIX = "wx_miniapp_token";
    private static final long CACHE_EXPIRE_SECONDS = 6600;

    /**
     * 获取微信AccessToken（优先从缓存获取）
     *
     * @return AccessToken，获取失败返回null
     */
    public String getAccessToken() {
        String cacheKey = getCacheKey();
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String cachedToken = stringRedisTemplate.boundValueOps(cacheKey).get();
        if (!StringUtils.isEmpty(cachedToken)) {
            return cachedToken;
        }

        return fetchAndCacheToken(false);
    }

    /**
     * 强制刷新AccessToken（忽略缓存）
     *
     * @return 新的AccessToken，获取失败返回null
     */
    public String refreshAccessToken() {
        return fetchAndCacheToken(true);
    }

    /**
     * 获取缓存key
     *
     * @return 缓存key
     */
    private String getCacheKey() {
        return CACHE_KEY_PREFIX;
    }

    /**
     * 调用微信API获取Token并缓存
     *
     * @param forceRefresh 是否强制刷新
     * @return AccessToken，获取失败返回null
     */
    private String fetchAndCacheToken(boolean forceRefresh) {
        log.info("用户进入token认证appid为{}",appid);
        Map<String, Object> request = new HashMap<>();
        request.put("grant_type", "client_credential");
        request.put("appid", appid);
        request.put("secret", secret);
        request.put("force_refresh", forceRefresh);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        try {
            String responseStr = restTemplate.postForObject(
                    wxUrl + "cgi-bin/stable_token",
                    new HttpEntity<>(request, headers),
                    String.class
            );
            log.info("获取用户accetoken返回{}", responseStr);
            if (responseStr != null) {
                JSONObject response = JSON.parseObject(responseStr);
                String accessToken = response.getString("access_token");
                if (accessToken != null) {
                    stringRedisTemplate.boundValueOps(getCacheKey()).set(accessToken, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
                    return accessToken;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}