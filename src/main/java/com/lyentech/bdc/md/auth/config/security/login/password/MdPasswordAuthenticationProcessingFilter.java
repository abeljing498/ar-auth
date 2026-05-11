package com.lyentech.bdc.md.auth.config.security.login.password;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.common.constant.LoginResultType;
import com.lyentech.bdc.md.auth.config.security.login.exception.*;
import com.lyentech.bdc.md.auth.dao.MdAppLoginMethodMapper;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdAppLoginMethod;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdAfsParam;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import com.lyentech.bdc.md.auth.tencent.service.AfsService;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.lyentech.bdc.md.auth.common.constant.MdAfsConstant.RAND_STR;
import static com.lyentech.bdc.md.auth.common.constant.MdAfsConstant.TICKET;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.PASSWORD;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.CHEMICAL_PLANT_URL;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.MD_LOGIN_BODY;
import static com.lyentech.bdc.md.auth.common.constant.MdPasswordConstant.USER_PW;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.*;
import static com.lyentech.bdc.md.auth.tencent.service.AfsService.AFS_SUCCESS;
import static com.lyentech.bdc.md.auth.tencent.service.AfsService.TICKET_EXPIRED;

/**
 * @author guolanren
 */
public class MdPasswordAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {

    public static final String PASSWORD_ERROR = "PASSWORD:ERROR:";
    private boolean postOnly = true;
    private AfsService afsService;

    public MdPasswordAuthenticationProcessingFilter(AfsService afsService) {
        super();
        this.afsService = afsService;
    }


    @Override
    public void setFilterProcessesUrl(String filterProcessesUrl) {
        super.setFilterProcessesUrl(filterProcessesUrl);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !HttpMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationServiceException("/login 不支持该请求方法: " + request.getMethod());
        }

        MdLoginParam loginParam = obtainLoginParam(request);
        // 是否是密码方式登录
        String authType = loginParam.getAuthType();
        if (authType == null || PASSWORD.equals(authType)) {
            String returnTo = null;

            try {
                returnTo = URLDecoder.decode(loginParam.getReturnTo(), "UTF-8");
                Integer isCheck = afsService.check(request);
                if (isCheck != AFS_SUCCESS) {
                    throw new MdAfsCheckException("滑块验证码已失效，请重新验证！");

                }
            } catch (UnsupportedEncodingException e) {
                logger.error("回调地址解码失败！", e);
                throw new MdLoginSsoErrorException("回调地址解码失败");
            } catch (TencentCloudSDKException e) {
                logger.error("滑动验证失败！", e);
                throw new MdLoginPasswordErrorException("滑动验证失败！");
            }
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(returnTo).build();
            List<String> clients = uriComponents.getQueryParams().get("client_id");
            String clientId = clients.get(0);
            String account = loginParam.getAccount();
            String password = loginParam.getPassword();
            StringBuilder stringBuilder = new StringBuilder();
            Authentication authentication;
            MdUserMapper mdUserMapper = SpringContextUtil.getBean(MdUserMapper.class);
            MdLoginLogMapper mdLoginLogMapper = SpringContextUtil.getBean(MdLoginLogMapper.class);
            //添加登录失败日志
            MdLoginLog loginLog = new MdLoginLog();
            loginLog.setLoginWay(loginParam.getAuthType());
            loginLog.setOperateSystem(loginParam.getLoginSystem());
            loginLog.setBrowser(loginParam.getLoginBrowser());
            loginLog.setIp(ServletUtil.getClientIP(request));
            loginLog.setAppId(clientId);
            //判断账号是否存在，判断账号是否已被加入黑名单
            MdUser byPhone = mdUserMapper.searchByPhone(account);
            Long accountId;
            if (ObjectUtils.isEmpty(byPhone)) {
                MdUser byAccount = mdUserMapper.searchByAccount(account);
                if (ObjectUtils.isEmpty(byAccount)) {
                    loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
                    loginLog.setAccount(account);
                    loginLog.setFailReason("用户不存在");
                    mdLoginLogMapper.insert(loginLog);
                    throw new MdLoginPasswordErrorException("密码错误或用户不存在！");
                } else {
                    accountId = byAccount.getId();
                }
            } else {
                accountId = byPhone.getId();
            }
            //初始化登录次数
            AtomicInteger errorNum = new AtomicInteger(0);
            StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
            String errorKey = stringBuilder.append(PASSWORD_ERROR).append(accountId).toString();
            String errorValue = stringRedisTemplate.boundValueOps(errorKey).get();
            if (errorValue != null) {
                errorNum = new AtomicInteger(Integer.parseInt(errorValue));
            }
            if (errorNum.longValue() >= 5) {
                //如果用户错误登录次数超过十次
                Long expire = stringRedisTemplate.getExpire(errorKey);
                MdAppLoginMethodMapper mdAppLoginMethodMapper = SpringContextUtil.getBean(MdAppLoginMethodMapper.class);
                //抛出账号锁定异常类
                MdAppLoginMethod loginMethod = mdAppLoginMethodMapper.selectOne(Wrappers.<MdAppLoginMethod>lambdaQuery().eq(MdAppLoginMethod::getAppId, clientId));
                if (loginMethod.getAccount().equals(true) && loginMethod.getCode().equals(false) && loginMethod.getSso().equals(false)) {
                    throw new MdLoginErrorLockException("该账号密码错误次数已到达上限,请在" + expire / 60 + "分钟" + expire % 60 + "秒后重试 ");
                } else {
                    throw new MdLoginErrorLockException("该账号密码错误次数已到达上限,请在" + expire / 60 + "分钟" + expire % 60 + "秒后重试或使用其他验证方式登录 ");
                }

            }
            if (account == null) {
                account = "";
            }

            if (password == null) {
                password = "";
            }

            account = account.trim();

            MdPasswordAuthenticationToken authRequest;
            try {
                authRequest = new MdPasswordAuthenticationToken(account, password);
            } catch (IllegalArgumentException e) {
                loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
                loginLog.setAccount(account);
                loginLog.setFailReason("密码错误");
                mdLoginLogMapper.insert(loginLog);
                throw new MdLoginParamIllegalException(e.getMessage());
            }
            setDetails(request, authRequest);
            try {
                authentication = this.getAuthenticationManager().authenticate(authRequest);
                stringRedisTemplate.delete(errorKey);
            } catch (InternalAuthenticationServiceException | BadCredentialsException e) {
                stringRedisTemplate.boundValueOps(errorKey).set(String.valueOf(errorNum.incrementAndGet()), 1800, TimeUnit.SECONDS);
                loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
                loginLog.setAccount(account);
                loginLog.setFailReason("密码错误");
                mdLoginLogMapper.insert(loginLog);
                throw new MdLoginPasswordErrorException("密码错误或用户不存在！");
            }
            String defaultReturnUri = (String) request.getAttribute(CHEMICAL_PLANT_URL);
            String forwardUri = defaultReturnUri;
            if (loginParam != null && loginParam.getReturnTo() != null) {
                try {
                    forwardUri = URLDecoder.decode(loginParam.getReturnTo(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            MdUser mdUser = (MdUser) authentication.getPrincipal();
            //判断该用户是否在黑名单中
            MdBlackUserMapper mdBlackUserMapper = SpringContextUtil.getBean(MdBlackUserMapper.class);
            MdBlackUser mdBlackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                    .eq(MdBlackUser::getUserId, mdUser.getId()));
            if (ObjectUtils.isNotEmpty(mdBlackUser)) {
                loginLog.setIsSuccess(LoginResultType.FAIL.getCode());
                loginLog.setAccount(account);
                loginLog.setFailReason("用户为黑名单用户！");
                mdLoginLogMapper.insert(loginLog);
                throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "无权登录系统，请联系管理员");
            }
            //查看用户的密码是否到期
            String userId = stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).get();
            //重定向到重置密码界面
            MdUser user = mdUserMapper.selectById(mdUser.getId());
            if (StringUtils.isEmpty(userId)) {
                //查看用户是否是首次账号密码登录
                if (!user.getFirstLoginPW()) {
                    throw new MdLoginPasswordFirstException("该账号使用初始密码登录，请重新设置用户密码！");
                }
                throw new MdLoginPasswordTimeOutException("该密码已过期，请重新设置！");
            }
            //密码方式登录成功添加第一次密码登录为 true
            if (!user.getFirstLoginPW()) {
                user.setFirstLoginPW(true);
                mdUserMapper.updateById(user);
            }
            StringBuilder authBuilder = new StringBuilder();
            String authKey = authBuilder.append(USER_CHANGE_PATH)
                    .append(mdUser.getId()).append(":")
                    .append("client:").append(clientId).toString();
            stringRedisTemplate.delete(authKey);
            return authentication;
        } else {
            throw new MdUnsupportLoginAuthTypeException("不支持的登录认证方式：" + authType + "。支持 msm、password，默认 password");
        }
    }

    protected MdLoginParam obtainLoginParam(HttpServletRequest request) {
        MdLoginParam loginParam = (MdLoginParam) request.getAttribute(MD_LOGIN_BODY);
        return loginParam;
    }


}
