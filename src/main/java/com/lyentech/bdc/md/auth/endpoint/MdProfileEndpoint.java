package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.entity.MdAppLoginMethod;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.vo.HeadListVO;
import com.lyentech.bdc.md.auth.model.vo.MdUserVO;
import com.lyentech.bdc.md.auth.model.vo.TenantVO;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdProfileService;
import com.lyentech.bdc.md.auth.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author guolanren
 */
@RestController
public class MdProfileEndpoint {

    @Autowired
    private MdProfileService mdProfileService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private MdAppService mdAppService;
    /**
     * 获取登录用户信息
     *
     * @param auth2Authentication
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @GetMapping("/me")
    public ResultEntity me(OAuth2Authentication auth2Authentication) {

        Object principal = auth2Authentication.getPrincipal();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;
            String appKey = auth2Authentication.getOAuth2Request().getClientId();
            user = mdProfileService.getMe(appKey, user);
            return ResultEntity.success(user);
        }
        return ResultEntity.success(principal);
    }
    /**
     * 获取登录用户基本信息
     *
     * @param auth2Authentication
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @GetMapping("/userInfo")
    public ResultEntity userInfo(OAuth2Authentication auth2Authentication) {

        Object principal = auth2Authentication.getPrincipal();
        MdUserVO mdUserVO = new MdUserVO();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;
            BeanUtils.copyProperties(user, mdUserVO);
            return ResultEntity.success(mdUserVO);
        }
        return ResultEntity.success();
    }


    @PreAuthorize("#oauth2.hasScope('profile')")
    @GetMapping("/roleInfo")
    public ResultEntity roleInfo(OAuth2Authentication auth2Authentication, @RequestParam("tenantId") Long tenantId) {

        Object principal = auth2Authentication.getPrincipal();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;
            String appKey = auth2Authentication.getOAuth2Request().getClientId();
            Map roles = mdProfileService.getRoleInfo(appKey, tenantId, user.getId());
            return ResultEntity.success(roles);
        }
        return ResultEntity.success(Collections.emptySet());

    }

    @PreAuthorize("#oauth2.hasScope('profile')")
    @GetMapping("/roles")
    public ResultEntity roles(OAuth2Authentication auth2Authentication, @RequestParam("tenantId") Long tenantId) {

        Object principal = auth2Authentication.getPrincipal();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;
            String appKey = auth2Authentication.getOAuth2Request().getClientId();
            Set<String> roles = mdProfileService.getRoles(appKey, tenantId, user.getId());
            return ResultEntity.success(roles);
        }
        return ResultEntity.success(Collections.emptySet());

    }


    /**
     * 获取用户在指定app下的租户
     *
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @GetMapping("/userTenant/getList")
    public ResultEntity getList(OAuth2Authentication auth2Authentication) {
        Object principal = auth2Authentication.getPrincipal();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;

            String appKey = auth2Authentication.getOAuth2Request().getClientId();
            List<TenantVO> tenantVOS = tenantService.getListByAppKeyAndUserId(appKey, user.getId());
            return ResultEntity.success(tenantVOS);
        }
        return ResultEntity.success(Collections.emptyList());

    }
}
