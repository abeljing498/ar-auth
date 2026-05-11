package com.lyentech.bdc.md.auth.endpoint;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.kr.starter.constant.KrSmsType;
import com.lyentech.bdc.kr.starter.service.KrInfoPush;
import com.lyentech.bdc.md.auth.common.exception.MdVerificationCodeException;
import com.lyentech.bdc.md.auth.model.entity.MdAppLoginMethod;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdResetPassword;
import com.lyentech.bdc.md.auth.model.param.MdUserBindParam;
import com.lyentech.bdc.md.auth.model.param.MdUserParam;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 账号相关接口
 *
 * @author guolanren
 */
@RestController
@RequestMapping("/account")
public class MdAccountEndpoint {

    @Autowired
    private MdUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private KrInfoPush krInfoPush;
    @Autowired
    MdAppService mdAppService;

    /**
     * 提供给当前登录用户的：用户更新 接口
     * 需要提供用户 token
     *
     * @param auth2Authentication
     * @param userParam
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/update")
    public ResultEntity update(OAuth2Authentication auth2Authentication, @RequestBody MdUserParam userParam) {
        Object principal = auth2Authentication.getPrincipal();
        if (principal instanceof MdUser) {
            MdUser user = (MdUser) principal;
            if (user.getId().equals(userParam.getId())) {
                userService.update(userParam);
            }
        }
        return ResultEntity.success();
    }

    /**
     * 修改密码
     *
     * @param resetPassword 手机号；旧密码；新密码
     * @return
     */
    @PostMapping("/reset_pw")
    public ResultEntity resetPassword(@RequestBody MdResetPassword resetPassword) {
        userService.resetPassword(resetPassword);
        return ResultEntity.success();
    }

    /**
     * 验证码方式修改密码
     *
     * @param resetPassword 手机号；验证码；新密码
     * @return
     */
    @PostMapping("/code_reset_pw")
    public ResultEntity codeResetPassword(@RequestBody MdResetPassword resetPassword) {
        userService.codeResetPassword(resetPassword);
        return ResultEntity.success();
    }

    /**
     * 管理员修改密码
     * @param resetPassword
     * @return
     */
    @PostMapping("/resetPwByManager")
    public ResultEntity resetPwByManager(@RequestBody MdResetPassword resetPassword) {
         String initPassword=userService.resetPwByManager(resetPassword);
        return ResultEntity.success(initPassword);
    }

    /**
     * 登录页修改密码
     * @param resetPassword
     * @return
     */
    /**
     * 校验验证码
     *
     * @param
     * @return
     */
    @PostMapping("/checkCode")
    public ResultEntity checkCode(@RequestBody MdResetPassword resetPassword) {
        if (StringUtils.isBlank(resetPassword.getCode())) {
            throw new IllegalParamException("验证码不能为空");
        }
        if (StringUtils.isBlank(resetPassword.getPhone())) {
            throw new IllegalParamException("手机号不能为空");
        }

        // 验证手机验证码
//        String smsCodeRedisKey = String.format(MD_AUTH_SCENE_VERIFICATION_PHONE_REDIS_KEY_PREFIX, "reset", resetPassword.getPhone());
//        String smsCode = stringRedisTemplate.boundValueOps(smsCodeRedisKey).get();
        Boolean isRight = krInfoPush.verifySmsCode(resetPassword.getPhone(), resetPassword.getCode(), KrSmsType.LOGIN);

        if (!isRight) {
            throw new MdVerificationCodeException("验证码错误");
        }
//        // 验证成功删除验证码
//        stringRedisTemplate.delete(smsCodeRedisKey);
        return ResultEntity.success();
    }


    @PostMapping("/bindPhone")
    public ResultEntity bindPhone(@RequestBody MdUserBindParam userBindParam) {
        userService.bindUser(userBindParam);
        return ResultEntity.success();
    }
    @GetMapping("/getAppLoginMethod")
    public ResultEntity getAppLoginMethod(String appKey) throws IOException {
        MdAppLoginMethod mdAppLoginMethod=mdAppService.getAppLoginMethod(appKey);
        return ResultEntity.success(mdAppLoginMethod);
    }
}
