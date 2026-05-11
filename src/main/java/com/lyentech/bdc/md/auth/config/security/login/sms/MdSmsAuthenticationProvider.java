package com.lyentech.bdc.md.auth.config.security.login.sms;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.kr.starter.constant.KrSmsType;
import com.lyentech.bdc.kr.starter.service.KrInfoPush;
import com.lyentech.bdc.md.auth.common.constant.LoginResultType;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSmsErrorException;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginOutLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.SMS;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.*;
/**
 * @author guolanren
 */
public class MdSmsAuthenticationProvider implements AuthenticationProvider {

    private MdUserService userService;
    private KrInfoPush krInfoPush;
    private StringRedisTemplate stringRedisTemplate;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    public MdSmsAuthenticationProvider(MdUserService userService, StringRedisTemplate stringRedisTemplate, KrInfoPush krInfoPush) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.krInfoPush = krInfoPush;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdSmsAuthenticationToken authenticationToken = (MdSmsAuthenticationToken) authentication;
        MdLoginLogMapper mdLoginLogMapper = SpringContextUtil.getBean(MdLoginLogMapper.class);
        String phone = (String) authenticationToken.getPrincipal();
        String clientId =  authenticationToken.getClientId();
        String loginBrowser =  authenticationToken.getLoginBrowser();
        String userIp =  authenticationToken.getUserIp();
        String loginSystem =  authenticationToken.getLoginSystem();
        MdLoginLog loginLog = new MdLoginLog();
        loginLog.setLoginWay(SMS);
        loginLog.setOperateSystem(loginSystem);
        loginLog.setBrowser(loginBrowser);
        loginLog.setIp(userIp);
        loginLog.setAppId(clientId);
        // 手机号未注册则登录失败
        MdUser userDetails = userService.getByPhone(phone);
        if (userDetails == null) {
            loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
            loginLog.setAccount(phone);
            loginLog.setFailReason("用户不存在");
            mdLoginLogMapper.insert(loginLog);
            throw new MdLoginSmsErrorException("用户不存在！");
        }
        // 验证手机验证码
        Boolean isRight = krInfoPush.verifySmsCode(phone, authenticationToken.getSmsCode(), KrSmsType.LOGIN);
        //旧版短信验证平台作废
        if (!isRight) {
            loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
            loginLog.setAccount(phone);
            loginLog.setFailReason("验证码输入不正确或已过期");
            mdLoginLogMapper.insert(loginLog);
            throw new MdLoginSmsErrorException("验证码输入不正确或已过期");
        }
        MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
        MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                .eq(MdBlackUser::getUserId, userDetails.getId()));
        if (ObjectUtils.isNotEmpty(mdBlackUser)) {
            loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
            loginLog.setAccount(phone);
            loginLog.setFailReason("用户为黑名单用户！");
            mdLoginLogMapper.insert(loginLog);
            throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
        }

        StringBuilder authBuilder = new StringBuilder();
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(userDetails.getId()).append(":")
                .append("client:").append(clientId).toString();
        //删除用户权限变更拦截
        stringRedisTemplate.delete(authKey);
        //删除管理员重置密码拦截
        stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + userDetails.getId().toString());
        return createSuccessAuthentication(userDetails, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdSmsAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal, authentication.getCredentials(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());

        return result;
    }
}
