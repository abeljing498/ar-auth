package com.lyentech.bdc.md.auth.config.security.login.sso;

import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginParamIllegalException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSsoErrorException;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author guolanren
 */
public class MdSsoAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ResultEntity resultEntity;
        try {
            throw exception;
        } catch (MdLoginParamIllegalException e) {
            resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
        } catch (MdLoginSsoErrorException e) {
            resultEntity = ResultEntity.faild(5002, null, e.getMessage());
        } catch (MdLoginBlackUserException e) {
            resultEntity = ResultEntity.faild(4004,null, e.getMessage());
        } catch (AuthenticationException e) {
            resultEntity = ResultEntity.faild(ResultCode.UNKNOWN_FAILED, e.getMessage());
        }
        HttpResponseUtil.setResultEntityAsContent(response, resultEntity);
    }
}
