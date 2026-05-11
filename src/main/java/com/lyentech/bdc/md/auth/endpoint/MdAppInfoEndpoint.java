package com.lyentech.bdc.md.auth.endpoint;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.dao.MdRoleMapper;
import com.lyentech.bdc.md.auth.dao.UserTenantRoleMapper;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.entity.UserTenantRole;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdBlackUserService;
import com.lyentech.bdc.md.auth.util.AuthorizationHeaderUtil;
import com.lyentech.bdc.md.auth.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdAuthTokenConstant.*;
import static com.lyentech.bdc.md.auth.util.JWTUtil.verifySecret;

/**
 * app 信息接口
 *
 * @author guolanren
 */
@RestController
@RequestMapping("/app")
public class MdAppInfoEndpoint {

    @Autowired
    private MdAppService appService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MdBlackUserService mdBlackUserService;
    @Autowired
    private UserTenantRoleMapper userTenantRoleMapper;
    @Autowired
    private MdRoleMapper mdRoleMapper;

    /**
     * 从认证服务器拉取app下的角色、菜单、权限信息
     *
     * @param authorization decode 后获取 appKey，appSecret
     * @return
     */
    @GetMapping("/acl")
    public ResultEntity acl(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String appKey = appKeyAndSecret[0];
        String appSecret = appKeyAndSecret[1];

        if (appService.isAuthorize(appKey, appSecret)) {
            return ResultEntity.success(appService.getTenantRoleInfo(appKey));
        } else {
            throw new MdAppAuthorizationException("Md App 认证失败，请检查 Key/Secret 是否正确");
        }
    }

    /**
     * Ar V1.8 对拉取权限机制进行了优化
     *
     * @param authorization
     * @return
     */
    @GetMapping("/clientAcl")
    public ResultEntity clientAcl(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String appKey = appKeyAndSecret[0];
        String appSecret = appKeyAndSecret[1];

        if (appService.isAuthorize(appKey, appSecret)) {
            return ResultEntity.success(appService.getClientRoleInfo(appKey));
        } else {
            throw new MdAppAuthorizationException("Md App 认证失败，请检查 Key/Secret 是否正确");
        }
    }

    @GetMapping("/roleAcl")
    public ResultEntity roleAcl(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String appKey = appKeyAndSecret[0];
        String appSecret = appKeyAndSecret[1];
        Long tenantId = Long.valueOf(appKeyAndSecret[2]);
        if (tenantId == -1) {
            String role = appKeyAndSecret[3];
            if (appService.isAuthorize(appKey, appSecret)) {
                return ResultEntity.success(appService.getAdminRoleAcl(appKey, role));
            } else {
                throw new MdAppAuthorizationException("Md App 认证失败，请检查 Key/Secret 是否正确");
            }
        } else if (tenantId == 0) {
            String role = appKeyAndSecret[3];
            if (appService.isAuthorize(appKey, appSecret)) {
                return ResultEntity.success(appService.getAdminRoleAcl(appKey, role));
            } else {
                throw new MdAppAuthorizationException("Md App 认证失败，请检查 Key/Secret 是否正确");
            }
        } else {
            Long roleId = Long.valueOf(appKeyAndSecret[3]);
            if (appService.isAuthorize(appKey, appSecret)) {
                return ResultEntity.success(appService.getTenantRoleAcl(appKey, tenantId, roleId));
            } else {
                throw new MdAppAuthorizationException("Md App 认证失败，请检查 Key/Secret 是否正确");
            }
        }
    }


    @GetMapping("/token")
    public ResultEntity getToken(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String tokenKey = appKeyAndSecret[0];
        Long userId = Long.valueOf(appKeyAndSecret[1]);
        String clientId = appKeyAndSecret[2];
        Long tenantId = Long.valueOf(appKeyAndSecret[3]);
        StringBuilder stringBuilder = new StringBuilder();
        String key = stringBuilder.append(TOKEN_PATH).append(tokenKey).toString();
        String s = stringRedisTemplate.boundValueOps(key).get();
        String type;
        StringBuilder authBuilder = new StringBuilder();
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(userId).append(":")
                .append("client:").append(clientId).toString();
        Boolean isBlack;
        if (StringUtils.isEmpty(s)) {
            String invaild = "MD_INVALID_TOKEN:" + tokenKey;
            Boolean exist = stringRedisTemplate.hasKey(invaild);
            if (exist.equals(true)) {
                stringRedisTemplate.delete(invaild);
                return ResultEntity.success("INVALID_TOKEN");
            }
            return null;
        } else {
            isBlack = mdBlackUserService.getIsBlack(clientId, userId);
        }
        if (isBlack.equals(false)) {
            Boolean hasKey = stringRedisTemplate.hasKey(authKey);
            if (hasKey) {
                Long changeTenantId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.boundValueOps(authKey).get()));
                if (changeTenantId.equals(tenantId)) {
                    type = "CHANGE_AUTH";
                } else {
                    type = "AUTH_PASS";
                }
            } else {
                type = "AUTH_PASS";
            }
        } else {
            type = "IS_BLACK";
        }
        /**
         * 用户账号默认密码修改之后需要退出登录
         */
        Boolean hasKey = stringRedisTemplate.hasKey(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + userId.toString());
        if (hasKey) {
            type = "MANAGER_CHANGE_PASSWORD";
        }
        return ResultEntity.success(type);
    }


    @GetMapping("/refresh")
    public ResultEntity refreshAuth(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String clientId = appKeyAndSecret[0];
        Long tenantId = Long.valueOf(appKeyAndSecret[1]);
        Long userId = Long.valueOf(appKeyAndSecret[2]);
        StringBuilder authBuilder = new StringBuilder();
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(userId).append(":")
                .append("client:").append(clientId).toString();
        Boolean hasKey = stringRedisTemplate.hasKey(authKey);
        if (hasKey) {
            Long changeTenantId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.boundValueOps(authKey).get()));
            if (changeTenantId.equals(tenantId)) {
                stringRedisTemplate.delete(authKey);
            }
        }
        return ResultEntity.success();

    }

    /**
     * 回调消息体，消费成功后删除消息记录
     *
     * @param authorization
     * @return
     */
    @GetMapping("/callReturnRole")
    public ResultEntity callReturnRole(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String appKey = appKeyAndSecret[0];
        String appSecret = appKeyAndSecret[1];
        String tenantId = appKeyAndSecret[2];
        String roleId = appKeyAndSecret[3];
        String status = appKeyAndSecret[4];
        Boolean isExist = stringRedisTemplate.opsForHash().hasKey("ar-roles:" + appKey.trim(), tenantId + "@" + roleId + "@" + status);
        if (isExist) {
            stringRedisTemplate.opsForHash().delete("ar-roles:" + appKey.trim(), tenantId + "@" + roleId + "@" + status);
        }
        return ResultEntity.success();
    }

    /**
     * 获取未消费的消息体
     *
     * @param authorization
     * @return
     */
    @GetMapping("/notConsumeMessages")
    public ResultEntity notConsumeMessages(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String appKey = appKeyAndSecret[0];
        String appSecret = appKeyAndSecret[1];
        Map<Object, Object> message = stringRedisTemplate.opsForHash().entries("ar-roles:" + appKey.trim());
        return ResultEntity.success(message);
    }

    /**
     * 获取负责人信息
     *
     * @param id
     * @return
     */
    @GetMapping("/getAppInfo")
    public ResultEntity getAppInfo(HttpServletRequest request,@RequestParam String id) throws IOException {
        return ResultEntity.success(appService.getAppInfo(request,id));
    }

    /**
     * 获取管理员设置密码提示信息
     *
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("/getAppManagerSetPasswordMsg")
    public ResultEntity getAppManagerSetPasswordMsg(@RequestParam String id) throws IOException {
        return ResultEntity.success(appService.getAppManagerSetPasswordMsg(id));
    }

    @GetMapping("/appPreAuthorize")
    public ResultEntity appPreAuthorize(@RequestParam String loginType, @RequestParam String keyword, @RequestParam String appKey) {
        return appService.appPreAuthorize(loginType, keyword, appKey);
    }

    @GetMapping("/credentialSign")
    public ResultEntity credentialSign(@RequestHeader("X-Authorization") String authorization,
                                       @RequestParam String message, HttpServletRequest request) {
        if (!authorization.isEmpty()) {
            String token = authorization;
            String appKey = JWTUtil.verifyKey(token);
            String secret = verifySecret(token);
            if (appKey != null && secret != null && appService.isAuthorize(appKey, secret)) {
                return ResultEntity.success(appService.credentialSign(appKey, message));
            } else {
                ResultEntity resultEntity = new ResultEntity();
                resultEntity.setCode(1000);
                resultEntity.setMsg("Ar认证失败 : 未获取到有效的认证信息");
                return resultEntity;
            }
        } else {
            return ResultEntity.success();
        }
    }

    @GetMapping("/getAuthorizationToken")
    public ResultEntity credentialSign(@RequestParam String grant_type, @RequestParam String app_key, @RequestParam String app_secret) {
        return ResultEntity.success(appService.getAuthorizationToken(grant_type, app_key, app_secret));
    }

    @GetMapping("/getUserListByRole")
    public ResultEntity getUserListByRole(@RequestHeader("Authorization") String authorization) {
        String[] appKeyAndSecret = AuthorizationHeaderUtil.obtainKeyAndSecretFromAuthorizationHeader(authorization);
        String clientId = appKeyAndSecret[0];
        Long tenantId = Long.valueOf(appKeyAndSecret[1]);
        if ("admin".equals(appKeyAndSecret[2])) {
            return ResultEntity.success();
        }
        Long roleId = Long.valueOf(appKeyAndSecret[2]);
        List<UserTenantRole> roles = userTenantRoleMapper.selectList(new QueryWrapper<UserTenantRole>().eq("app_id", clientId).eq("tenant_id", tenantId).eq("role_id", roleId).groupBy("user_id"));
        return ResultEntity.success(roles);

    }

    /**
     * 获取APP所有角色列表
     * @param @RequestParam String appId
     * @return
     */
    @GetMapping("/getAppRoleList")
    public ResultEntity getAppRoleList(@RequestParam String appId) {
        String clientId = appId;
        List<MdRole> roles = mdRoleMapper.selectList(new QueryWrapper<MdRole>().eq("app_id", clientId).eq("status", 1));
        return ResultEntity.success(roles);

    }

    /**
     * 更新角色版本
     * @param roleParam
     * @return
     */
    @PostMapping("/updateRoleVersion")
    public ResultEntity updateRole(@RequestBody RoleParam roleParam) {
        MdRole mdRole=null;
        if (!StringUtils.isEmpty(roleParam.getName())) {
             mdRole = mdRoleMapper.selectOne(Wrappers.<MdRole>lambdaQuery().eq(MdRole::getName, roleParam.getName()).eq(MdRole::getAppId,roleParam.getAppId()).last(" limit 1"));
        }else {
            mdRole = mdRoleMapper.selectOne(Wrappers.<MdRole>lambdaQuery().eq(MdRole::getId, roleParam.getId()));
        }
        if(mdRole!=null){
            mdRole.setVersion(mdRole.getVersion()+1);
            mdRole.setUpdateTime(new Date());
            mdRoleMapper.updateById(mdRole);
        }
        List<MdRole> roles = mdRoleMapper.selectList(new QueryWrapper<MdRole>().eq("app_id", roleParam.getAppId()).eq("status", 1));
        return ResultEntity.success(roles);
    }
}
