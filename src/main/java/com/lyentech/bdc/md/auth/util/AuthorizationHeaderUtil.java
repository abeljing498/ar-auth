package com.lyentech.bdc.md.auth.util;

import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.common.exception.MdObtainKeyAndSecretException;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author guolanren
 */
public class AuthorizationHeaderUtil {

    /**
     * 从指定的 header 头 {@code authorization} 中解码成对应的需要信息
     *
     * @param authorization
     * @return
     */
    public static String[] obtainKeyAndSecretFromAuthorizationHeader(String authorization) {
        // Authorization Header 必须以 basic（忽略大小写）开头
        if (authorization.toLowerCase().startsWith("basic ")) {
            // 截取 basic 后面那一串字符，解码
            String auth;
            try {
                byte[] base64Auth = authorization.substring(6).getBytes("UTF-8");
                byte[] decoded = Base64.getDecoder().decode(base64Auth);
                auth = new String(decoded, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MdObtainKeyAndSecretException("从 Authorization Header 获取 Key/Secret 失败", e);
            }

            // 检查 auth 是否包含分隔符 :
            int delimit = auth.indexOf(":");
            if (delimit == -1) {
                throw new MdAppAuthorizationException("Md App 认证失败，请检查 Authorization Header 是否符合标准: Basic base64({key}:{secret})");
            }

            // 使用 : 分隔符分隔，获取 key/secret
            String[] appKeyAndSecret = auth.split(":");

            return appKeyAndSecret;
        } else {
            throw new MdAppAuthorizationException("Md App 认证失败，请检查 Authorization Header 是否符合标准: Basic base64({key}:{secret})");
        }
    }
    /**
     * 从指定的 header 头 {@code authorization} 中解码成对应的需要信息
     *
     * @param accessToken
     * @return
     */
    public static String[] obtainKeyAndSecretFromTokenHeader(String accessToken) {
        // Authorization Header 必须以 basic（忽略大小写）开头
        if (accessToken.toLowerCase().startsWith("Bearer ")) {
            // 截取 basic 后面那一串字符，解码
            String auth;
            try {
                byte[] base64Auth = accessToken.substring(6).getBytes("UTF-8");
                byte[] decoded = Base64.getDecoder().decode(base64Auth);
                auth = new String(decoded, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MdObtainKeyAndSecretException("Ar认证服务 ：从 Authorization Header 获取 Key/Secret 失败", e);
            }

            // 检查 auth 是否包含分隔符 :
            int delimit = auth.indexOf(":");
            if (delimit == -1) {
                throw new MdAppAuthorizationException("Md App 认证失败，请检查 Authorization Header 是否符合标准: Basic base64({key}:{secret})");
            }

            // 使用 : 分隔符分隔，获取 key/secret
            String[] appKeyAndSecret = auth.split(":");

            return appKeyAndSecret;
        } else {
            throw new MdAppAuthorizationException("Md App 认证失败，请检查 Authorization Header 是否符合标准: Basic base64({key}:{secret})");
        }
    }
}
