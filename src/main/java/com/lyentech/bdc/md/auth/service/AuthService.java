package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.md.auth.model.entity.Auth;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.md.auth.model.vo.AuthOldVO;
import com.lyentech.bdc.md.auth.model.vo.AuthVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface AuthService extends IService<Auth> {

    /**
     * 获取app下所有权限
     * @param appId
     * @return
     */
    List<AuthVO> getAllAuth(String appId);

    List<AuthOldVO> getAllAuthOld(String appId);
}
