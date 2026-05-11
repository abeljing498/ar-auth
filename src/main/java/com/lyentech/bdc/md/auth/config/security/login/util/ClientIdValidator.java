package com.lyentech.bdc.md.auth.config.security.login.util;

import com.lyentech.bdc.md.auth.dao.MdAppMapper;
import com.lyentech.bdc.md.auth.model.entity.MdApp;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.springframework.util.StringUtils;

/**
 * Client ID 校验工具类
 * 用于验证client_id是否为已注册的应用
 *
 * @author security-fix
 */
public class ClientIdValidator {

    /**
     * 验证client_id是否为已注册的应用
     *
     * @param clientId 待验证的client_id
     * @return true=有效, false=无效
     */
    public static boolean isValidClientId(String clientId) {
        if (StringUtils.isEmpty(clientId)) {
            return false;
        }

        MdAppMapper mdAppMapper = SpringContextUtil.getBean(MdAppMapper.class);
        MdApp app = mdAppMapper.getById(clientId);
        return app != null;
    }

    /**
     * 验证并获取client_id，无效时抛出异常
     *
     * @param clientId 待验证的client_id
     * @return 验证通过的clientId
     * @throws IllegalArgumentException 如果client_id无效
     */
    public static String validateAndGet(String clientId) {
        if (!isValidClientId(clientId)) {
            throw new IllegalArgumentException("无效的客户端标识");
        }
        return clientId;
    }
}