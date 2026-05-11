package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.model.entity.Auth;
import com.lyentech.bdc.md.auth.dao.AuthMapper;
import com.lyentech.bdc.md.auth.model.vo.AuthOldVO;
import com.lyentech.bdc.md.auth.model.vo.AuthVO;
import com.lyentech.bdc.md.auth.service.AuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth> implements AuthService {

    @Resource
    private AuthMapper authMapper;

    @Override
    public List<AuthVO> getAllAuth(String appId) {
        List<AuthVO> voList = authMapper.getGroupAuth(appId);
        return voList;
    }

    @Override
    public List<AuthOldVO> getAllAuthOld(String appId) {
        List<Auth> authVOList = authMapper.selectList(Wrappers.<Auth>lambdaQuery().eq(Auth::getAppId,appId));
        List<AuthOldVO> voList = new ArrayList<>();
        for (Auth auth:authVOList) {
            AuthOldVO authVO = new AuthOldVO();
            authVO.setName(auth.getName());
            authVO.setId(auth.getId());
            authVO.setStatus(auth.getStatus());
            voList.add(authVO);
        }
        return voList;
    }
}
