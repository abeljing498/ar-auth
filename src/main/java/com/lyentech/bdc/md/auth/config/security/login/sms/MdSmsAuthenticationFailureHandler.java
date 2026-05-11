package com.lyentech.bdc.md.auth.config.security.login.sms;

import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginParamIllegalException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSignErrorException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSmsErrorException;
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
public class MdSmsAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ResultEntity resultEntity;
        try {
            throw exception;
        } catch (MdLoginParamIllegalException e) {
            resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
        } catch (MdLoginSmsErrorException e) {
            resultEntity = ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
        } catch (MdLoginBlackUserException e) {
            resultEntity = ResultEntity.faild(4004, null, e.getMessage());
        } catch (MdLoginSignErrorException e) {
            resultEntity = ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
        }
        catch (AuthenticationException e) {
            resultEntity = ResultEntity.faild(ResultCode.UNKNOWN_FAILED, e.getMessage());
        }
        HttpResponseUtil.setResultEntityAsContent(response, resultEntity);
    }
}
