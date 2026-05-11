package com.lyentech.bdc.md.auth.endpoint.open;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.dao.MdAppMapper;
import com.lyentech.bdc.md.auth.model.param.DeleteUserParam;
import com.lyentech.bdc.md.auth.model.param.ManagerResetPasswordParam;
import com.lyentech.bdc.md.auth.model.param.MdResetPassword;
import com.lyentech.bdc.md.auth.model.param.OrgUserParam;
import com.lyentech.bdc.md.auth.model.vo.CreateSingleVO;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.OrgService;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.APP_KEY_PARAM;
import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.KEY_SIGN_PARAM;

/**
 * 外部人员接口
 *
 * @author YuYi
 * @create 2022/8/31
 * @create 10:46
 */
@RestController
@RequestMapping("/external/userOrg")
public class ExternalUserEndpoint {

    @Autowired
    private MdAppService appService;

    @Autowired
    private OrgUserService orgUserService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private MdUserService mdUserService;
    @Resource
    MdAppMapper mdAppMapper;
    /**
     * 外部接口：添加人员
     *
     * @param body  签名
     * @param param
     * @return
     */
    @PostMapping("/add")
    public ResultEntity addUser(@RequestBody String body, @RequestParam Map<String, String> param) throws Exception {
        String appKey = param.get(APP_KEY_PARAM);
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgUserParam orgUserParam = JSONObject.parseObject(body, OrgUserParam.class);
            Long orgId = orgUserParam.getOrgId();
            Long tenantId = orgService.getTenantId(orgId);
            HashSet hashSet = new HashSet();
            hashSet.add(orgId);
            orgUserParam.setOrgIds(hashSet);
            Set<Long> roleList = new HashSet<>();
            orgUserParam.setRoleList(roleList);
            orgUserParam.setAppId(appKey);
            orgUserParam.setTenantId(tenantId);
            return ResultEntity.success(orgUserService.addExternalUser(orgUserParam));
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 外部接口：传递一个组织id，批量删除
     *
     * @param param
     * @param body
     * @return
     */
    @PostMapping("/delete")
    public ResultEntity deleteUser(@RequestBody String body,
                                   @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            DeleteUserParam deleteUserParam = JSONObject.parseObject(body, DeleteUserParam.class);
            orgUserService.deleteUser(deleteUserParam);
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 外部接口：管理员修改密码(只有管理员可重置密码)
     *
     * @return
     */
    @PostMapping("/update_pw")
    public ResultEntity updatePassword(@RequestBody String body,
                                       @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))){
            ManagerResetPasswordParam managerResetPasswordParam = JSONObject.parseObject(body, ManagerResetPasswordParam.class);
            managerResetPasswordParam.setAppKey(param.get(APP_KEY_PARAM));
            //重置密码
            return ResultEntity.success(mdUserService.managerResetPassword(managerResetPasswordParam));
        }else {
            throw new MdAppAuthorizationException("签名失败");
        }

    }
    @PostMapping("/send_single")
    public ResultEntity sendSingle(@RequestBody String body,
                                   @RequestParam Map<String, String> params){
        Long dateTime = System.currentTimeMillis();
        System.out.println(dateTime);
        String sp = "MdUser{id=1165, phone='null', password='$2a$10$.gjC/BgjIpFOArvTwH0SVuNq/Y4YeWrtkSJtYoMQSLLVz5k8GNzia', nickname='小鸭', avatar='null', email='null', deleted=false, roles=null, createTime=Wed Nov 23 10:06:46 CST 2022, updateTime=Thu Nov 24 15:15:50 CST 2022, additional=null}";
        System.out.println(sp.length());
        params.put("timestamp",String.valueOf(dateTime));
        StringBuilder signEntity = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            signEntity.append(params.keySet().stream().filter(key -> !"signature".equals(key))
                    .map(key -> key + params.get(key))
                    .collect(Collectors.joining())
            );}

        if (!StringUtils.isEmpty(body)) {
            signEntity.append(body);
        }
        String secret = mdAppMapper.getSecret(params.get(APP_KEY_PARAM));
        signEntity.append(secret);
        System.out.println(signEntity);
        String str = DigestUtils.md5DigestAsHex(signEntity.toString().getBytes(StandardCharsets.UTF_8)).toUpperCase();
        System.out.println(str);
        CreateSingleVO createSingleVO = new CreateSingleVO();
        createSingleVO.setSignature(str);
        createSingleVO.setTimestamp(dateTime);
        return ResultEntity.success(createSingleVO);
    }

    /**
     * 获取用户信息
     * @param account
     * @return
     */
    @GetMapping("/getUserInfoByAccount")
    public ResultEntity getUserInfoByAccount(@RequestParam String account){
        return ResultEntity.success(orgUserService.getUserInfoByAccount(account));
    }

    /**
     * 通过用户手机号修改密码
     * @param resetPassword
     * @return
     */
    @PostMapping("/resetPwByPhone")
    public ResultEntity resetPwByPhone(@RequestBody MdResetPassword resetPassword, HttpServletRequest request) {
        String ip = ServletUtil.getClientIP(request);
        resetPassword.setUserIp(ip);
        String initPassword=mdUserService.resetPwByPhone(resetPassword);
        return ResultEntity.success(initPassword);
    }
}
