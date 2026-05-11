package com.lyentech.bdc.md.auth.endpoint.open;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.model.entity.MdUserOrg;
import com.lyentech.bdc.md.auth.model.param.OrgUserParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgUserParam;
import com.lyentech.bdc.md.auth.model.param.UserTenantRoleParam;
import com.lyentech.bdc.md.auth.model.vo.PasswordVO;
import com.lyentech.bdc.md.auth.model.vo.UserDetailsVO;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdUserOrgService;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.service.UserTenantRoleService;
import com.lyentech.bdc.md.auth.util.JWTUtil;
import com.lyentech.bdc.md.auth.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.APP_KEY_PARAM;
import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.KEY_SIGN_PARAM;
import static com.lyentech.bdc.md.auth.util.JWTUtil.verifySecret;

/**
 * @Author :yan
 * @Date :Create in 2022/9/19
 * @Description : 用户组织开放接口
 */

@RestController
@RequestMapping("/open/OrgUser")
public class OpenOrgUserEndpoint {
    @Autowired
    private MdAppService appService;
    @Autowired
    private OrgUserService orgUserService;
    @Autowired
    private MdUserOrgService mdUserOrgService;
    @Autowired
    private UserTenantRoleService userTenantRoleService;

    /**
     * 在指定租户和组织下关联用户，如果用户不存在则先创建用户
     *
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/add")
    public ResultEntity add(@RequestBody String body, @RequestParam Map<String, String> param) throws Exception {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgUserParam orgUserParam = JSONObject.parseObject(body, OrgUserParam.class);
            orgUserParam.setAppId(appKey);
            if (orgUserParam.getTenantId() == null || StringUtils.isEmpty(orgUserParam.getNickname())) {
                throw new IllegalParamException("租户id、用户名不能为空！");
            }
            if (CollectionUtils.isEmpty(orgUserParam.getOrgIds())) {
                throw new IllegalParamException("组织id不能为空！");
            }
            return ResultEntity.success(orgUserService.add(orgUserParam));
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/update")
    public ResultEntity update(@RequestBody String body, @RequestParam Map<String, String> param) throws Exception {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgUserParam orgUserParam = JSONObject.parseObject(body, OrgUserParam.class);
            orgUserParam.setAppId(appKey);
            if (orgUserParam.getId() == null || orgUserParam.getTenantId() == null) {
                throw new IllegalParamException("用户id和租户id不能为空！");
            }
            orgUserService.update(orgUserParam);
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/delete")
    public ResultEntity delete(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            JSONObject jsonObject = JSONObject.parseObject(body);
            Long id = jsonObject.getLong("id");
            Long orgId = jsonObject.getLong("orgId");
            if (id == null || orgId == null) {
                throw new IllegalParamException("用户id和组织id不能为空！");
            }
            mdUserOrgService.remove(Wrappers.<MdUserOrg>lambdaQuery()
                    .eq(MdUserOrg::getUserId, id)
                    .eq(MdUserOrg::getOrgId, orgId));
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getPage")
    public ResultEntity getPage(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            QueryOrgUserParam userParam = JSONObject.parseObject(body, QueryOrgUserParam.class);
            if (userParam.getPageNum() == null || userParam.getPageSize() == null) {
                throw new IllegalParamException("页数和页码不能为空");
            }
            if (userParam.getTenantId() == null) {
                throw new IllegalParamException("租户Id不能为空");
            }
//            userParam.setOrgId(userParam.getId());
            PageResult<UserDetailsVO> userList = orgUserService.getUserList(userParam.getPageNum(),
                    userParam.getPageSize(),
                    userParam.getTenantId(),
                    userParam.getOrgId(),
                    userParam.getAccount(),
                    userParam.getKeyword(),
                    appKey, userParam.getUserState());
            return ResultEntity.success(userList);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getDetail")
    public ResultEntity getDetail(@RequestBody(required = false) String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            QueryOrgUserParam userParam = JSONObject.parseObject(body, QueryOrgUserParam.class);
            if (userParam.getId() == null || userParam.getTenantId() == null ) {
                throw new IllegalParamException("用户Id和租户Id能为空");
            }
            UserDetailsVO userDetail = orgUserService.getUserDetail(userParam.getId(), null, userParam.getTenantId(), appKey, "");
            return ResultEntity.success(userDetail);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/addUserByAuthorization")
    public ResultEntity addUserByAuthorization(@RequestHeader("X-Authorization") String authorization,
                                               @RequestBody OrgUserParam orgUserParam, HttpServletRequest request) throws Exception {
            if (!authorization.isEmpty()) {
                String token = authorization;
                String appKey = JWTUtil.verifyKey(token);
                String secret = verifySecret(token);
                if (appKey != null && secret != null && appService.isAuthorize(appKey,secret)) {
                    orgUserParam.setAppId(appKey);
                    if (orgUserParam.getTenantId() == null || StringUtils.isEmpty(orgUserParam.getNickname())) {
                        throw new IllegalParamException("租户id、用户名和电话号码不能为空！");
                    }
                    if (CollectionUtils.isEmpty(orgUserParam.getOrgIds())) {
                        throw new IllegalParamException("组织id不能为空！");
                    }
                    PasswordVO passwordVO = orgUserService.add(orgUserParam);
                    if (ObjectUtils.isNotEmpty(orgUserParam.getRoleList()) && ObjectUtils.isNotEmpty(passwordVO)) {
                        UserTenantRoleParam userTenantRoleParam = new UserTenantRoleParam();
                        userTenantRoleParam.setUserId(passwordVO.getUserId());
                        userTenantRoleParam.setTenantId(orgUserParam.getTenantId());
                        userTenantRoleParam.setAppId(orgUserParam.getAppId());
                        List<Long> roleIds = new ArrayList<Long>(orgUserParam.getRoleList());
                        userTenantRoleParam.setRoleList(roleIds);
                        userTenantRoleService.addUserTenantRole(userTenantRoleParam);
                    }
                    return ResultEntity.success(passwordVO);
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

}
