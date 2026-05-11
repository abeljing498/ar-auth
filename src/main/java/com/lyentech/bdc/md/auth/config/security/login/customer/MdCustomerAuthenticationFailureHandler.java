package com.lyentech.bdc.md.auth.config.security.login.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginOutAuthErrorException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginParamIllegalException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSsoErrorException;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MdCustomerAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public MdCustomerAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        ResultEntity resultEntity;
        try {
            throw exception;
        } catch (AuthenticationServiceException e) {
            resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
        } catch (MdLoginSsoErrorException e) {
            resultEntity = ResultEntity.faild(5002, null, e.getMessage());
        } catch (MdLoginBlackUserException e) {
            resultEntity = ResultEntity.faild(4004,null, e.getMessage());
        } catch (AuthenticationException e) {
            resultEntity = ResultEntity.faild(5002,null, e.getMessage());
        }

        HttpResponseUtil.setResultEntityAsContent(response, resultEntity);
    }
} 