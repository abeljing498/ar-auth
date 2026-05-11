package com.lyentech.bdc.md.auth.util;



import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

/**
 * @author 260583
 */
public class JWTUtil {//过期时间
    private static final long EXPIRE_TIME = 120 * 60 * 1000;//默认15分钟
    //私钥
    private static final String TOKEN_SECRET = "privateKey";

    /**
     * 生成签名，15分钟过期
     * @param **username**
     * @param **password**
     * @return
     */
    public static String createToken(String appKey, String appSecret) {
        try {
            // 设置过期时间
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            // 私钥和加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            // 设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("Type", "Jwt");
            header.put("alg", "HS256");
            // 返回token字符串
            return JWT.create()
                    .withHeader(header)
                    .withClaim("appKey", appKey)
                    .withClaim("appSecret", appSecret)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成token，自定义过期时间 毫秒
     * @param **username**
     * @param **password**
     * @return
     */
    public static String createToken(String userId,long expireDate) {
        try {
            // 设置过期时间
            Date date = new Date(System.currentTimeMillis() + expireDate);
            // 私钥和加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            // 设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("Type", "Jwt");
            header.put("alg", "HS256");
            // 返回token字符串
            return JWT.create()
                    .withHeader(header)
                    .withClaim("userId", userId)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 检验token是否正确
     * @param **token**
     * @return
     */
    public static String verifyKey(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            String appKey = jwt.getClaim("appKey").asString();
            return appKey;
        } catch (Exception e){
            return null;
        }
    }

    public static String verifySecret(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            String appSecret = jwt.getClaim("appSecret").asString();
            return appSecret;
        } catch (Exception e){
            return null;
        }
    }

}

