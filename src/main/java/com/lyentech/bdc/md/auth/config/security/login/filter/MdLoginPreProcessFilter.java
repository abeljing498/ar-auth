package com.lyentech.bdc.md.auth.config.security.login.filter;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.CharStreams;
import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginPasswordErrorException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginUnknowDestinationException;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Collection;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.CHEMICAL_PLANT_URL;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.MD_LOGIN_BODY;

/**
 * @author guolanren
 */
@Slf4j
public class MdLoginPreProcessFilter extends AbstractMdSecurityFilter {

    /**
     * 全家桶地址(化学工厂)
     */
    private String chemicalPlantUrl;

    public MdLoginPreProcessFilter(String chemicalPlantUrl) {
        super(HttpMethod.POST, "/login");
        this.chemicalPlantUrl = chemicalPlantUrl;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (requiresAuthentication((HttpServletRequest) request)) {
            try {
                // 将全家桶地址（化学工厂）存在请求属性中
                request.setAttribute(CHEMICAL_PLANT_URL, chemicalPlantUrl);
                // 将登录的请求体（登录参数）存在请求属性中
                storeLoginParamInRequestAttribute(request);
                chain.doFilter(request, response);
            } catch (MdLoginUnknowDestinationException e) {
                log.info(e.toString());
                // 不知道该去哪的话，就去我们的全家桶网站吧...
                ResultEntity resultEntity = ResultEntity.redirect(chemicalPlantUrl);
                HttpResponseUtil.setResultEntityAsContent((HttpServletResponse) response, resultEntity);
            } catch (MdLoginPasswordErrorException e) {
                log.info(e.toString());
                ResultEntity resultEntity = ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
                HttpResponseUtil.setResultEntityAsContent((HttpServletResponse) response, resultEntity);
            } catch (IllegalArgumentException e) {
                log.info(e.toString());
                ResultEntity resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
                HttpResponseUtil.setResultEntityAsContent((HttpServletResponse) response, resultEntity);
            } catch (JSONException e) {
                log.info(e.toString());
                ResultEntity resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, "登录参数缺失或参数json格式不符合要求");
                HttpResponseUtil.setResultEntityAsContent((HttpServletResponse) response, resultEntity);
            } finally {

                removeLoginParamFromRequestAttribute(request);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void storeLoginParamInRequestAttribute(ServletRequest request) throws IOException, IllegalArgumentException, JSONException {
        request.setCharacterEncoding("UTF-8");
        String requestBody = CharStreams.toString(request.getReader());

        MdLoginParam loginParam = JSONObject.parseObject(requestBody, MdLoginParam.class);
        Assert.notNull(loginParam, "登录参数缺失");

        String phone = loginParam.getPhone();
        String base64Password = loginParam.getPassword();
        String smsCode = loginParam.getSmsCode();
        String authType = loginParam.getAuthType();
        String returnTo = loginParam.getReturnTo();
        String callback = loginParam.getCallback();
        String account = loginParam.getAccount();
        String credential = loginParam.getCredential();
        log.info("鉴权参数为：{}", JSONObject.toJSON(loginParam));
        if (returnTo == null) {
            throw new MdLoginUnknowDestinationException("登录参数重定向地址信息缺失");
        }

        if (SSO.equals(authType)) {
            Assert.notNull(callback, "单点登录callback信息错误");
        } else if (SMS.equals(authType)) {
            Assert.isTrue(phone.matches("1[3456789]\\d{9}"), "登录手机号信息错误");
            Assert.notNull(phone, "登录手机号信息错误");
            Assert.notNull(smsCode, "登录验证码信息错误");
            Assert.isTrue(smsCode.matches("\\d{6}"), "登录验证码信息错误");
        } else if (SIGN.equals(authType)) {
            Assert.notNull(credential, "签名参数缺失");
        } else if (OUTAUTH.equals(authType)) {
            Assert.notNull(callback, "第三方认证token不能为空！");
        } else if (SMSCUS.equals(authType)) {
            Assert.notNull(callback, "第三方认证token不能为空！");
        } else if (UNICOM.equals(authType)) {
            Assert.notNull(callback, "第三方认证token不能为空！");
        } else {
            log.info("没有适配的登录方式{}", authType);
            Assert.notNull(account, "登录帐号信息错误");
            Assert.notNull(base64Password, "登录参数密码信息缺失");
            String originPassword = new String(Base64.getDecoder().decode(base64Password));
            originPassword = URLDecoder.decode((originPassword), "UTF-8");
            loginParam.setPassword(originPassword);
        }
        request.setAttribute(MD_LOGIN_BODY, loginParam);
    }

    private void removeLoginParamFromRequestAttribute(ServletRequest request) {
        request.removeAttribute(MD_LOGIN_BODY);
    }


}
