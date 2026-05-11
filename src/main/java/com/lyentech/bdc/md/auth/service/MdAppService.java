package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.entity.MdAppLoginMethod;
import com.lyentech.bdc.md.auth.model.vo.AuthorizationTokenVO;
import com.lyentech.bdc.md.auth.model.vo.CredentialSignVO;
import com.lyentech.bdc.md.auth.model.vo.HeadListVO;
import com.lyentech.bdc.md.auth.model.vo.TenantRoleVO;
import org.springframework.security.oauth2.provider.ClientDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/**
 * @author guolanren
 */
public interface MdAppService extends ClientDetailsService {

    /**
     * 获取指定app下租户、角色、菜单、权限
     * @param appKey
     * @return
     */
    Set<TenantRoleVO> getTenantRoleInfo(String appKey);

    Set<TenantRoleVO> getClientRoleInfo(String appKey);

    TenantRoleVO getTenantRoleAcl(String appKey, Long tenantId, Long roleId);

    /**
     * 获取指定app下超管、匿名角色的菜单接口信息
     * @param appKey
     * @param role
     * @return
     */
    TenantRoleVO getAdminRoleAcl(String appKey, String role);


    /**
     * 验证 appKey 是否正确
     *
     * @param appKey
     * @param appSecret
     * @return
     */
    boolean isAuthorize(String appKey, String appSecret);

    /**
     * 获取指定 app 的主页
     *
     * @param appKey
     * @return
     */
    String getHomepage(String appKey);

    /**
     * 获取负责人信息
     *
     * @param id
     * @return
     */
//    @Cacheable(value = "au:app:acl")
    HeadListVO getAppInfo(HttpServletRequest request,String id) throws FileNotFoundException, IOException;

    /**
     * 获取下游应用登录方式
     * @param id
     * @return
     */
    MdAppLoginMethod getAppLoginMethod(String id);

    /**
     * 获取secret
     *
     * @param appKey appKey
     * @return secret
     */
    String getSecret(String appKey);

    ResultEntity appPreAuthorize(String loginType, String keyword, String appKey);

    /**
     * 免登录认证
     * @param appKey
     * @param message
     * @return
     */
    CredentialSignVO credentialSign(String appKey, String message);

    AuthorizationTokenVO getAuthorizationToken(String grantType, String appKey, String appSecret);

    String getAppManagerSetPasswordMsg(String id);
}
