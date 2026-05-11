package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.md.auth.model.entity.MdUser;

import java.util.Map;
import java.util.Set;

public interface MdProfileService {

    /**
     * 获取指定app下登陆用户的用户信息
     * @param appKey
     * @param user
     * @return
     */
    MdUser getMe(String appKey, MdUser user);

    /**
     * 获取指定app下登陆用户的用户角色
     * @param appKey
     * @param userId
     * @param tenantId
     * @return
     */
    Set<String> getRoles(String appKey, Long tenantId, Long userId);

    Map getRoleInfo(String appKey, Long tenantId, Long userId);

}
