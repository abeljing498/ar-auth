package com.lyentech.bdc.md.auth.config.security.login.password;

import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.*;
import com.lyentech.bdc.md.auth.tencent.exception.MdAfsException;
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
public class MdPasswordAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ResultEntity resultEntity;
        try {
            throw exception;
        } catch (MdLoginParamIllegalException e) {
            resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
        } catch (MdLoginPasswordErrorException | MdUnsupportLoginAuthTypeException e) {
            resultEntity = ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
        }catch (MdLoginPasswordTimeOutException e){
            ResultEntity result = new ResultEntity();
            result.setCode(6001);
            result.setMsg(e.getMessage());
            resultEntity = result;
        }catch (MdLoginPasswordFirstException e){
            ResultEntity result = new ResultEntity();
            result.setCode(6002);
            result.setMsg(e.getMessage());
            resultEntity = result;
        }catch (MdLoginErrorLockException e){
            ResultEntity result = new ResultEntity();
            result.setCode(6003);
            result.setMsg(e.getMessage());
            resultEntity = result;
        }catch (MdLoginBlackUserException e) {
            ResultEntity result = new ResultEntity();
            result.setCode(4004);
            result.setMsg(e.getMessage());
            resultEntity = result;
        }
        catch (MdAfsCheckException e) {
            ResultEntity result = new ResultEntity();
            result.setCode(4005);
            result.setMsg(e.getMessage());
            resultEntity = result;
        }
        catch (AuthenticationException e) {
            resultEntity = ResultEntity.faild(ResultCode.UNKNOWN_FAILED, e.getMessage());
        }
        HttpResponseUtil.setResultEntityAsContent(response, resultEntity);
    }
}
