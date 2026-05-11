package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.exception.*;
import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginErrorLockException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginPasswordFirstException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginPasswordTimeOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guolanren
 */
@ControllerAdvice
@ResponseBody
public class MdGlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MdGlobalExceptionHandler.class);

    /**
     * 2001
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity invalidParamHandle(InvalidParamException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.INVALID_PARAM_FAILED, e.getMessage());
    }

    /**
     * 2002
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity illegalParamHandle(IllegalParamException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
    }

    /**
     * 3000
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity serverErrorHandle(ServerErrorException e) {
        LOG.error(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.SERVER_ERROR, e.getMessage());
    }

    /**
     * 4001
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity tokenInvalidHandle(TokenInvalidException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.TOKEN_INVALID, e.getMessage());
    }

    /**
     * 4002
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity expiredSessionHandle(SessionInvalidException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.SESSION_INVALID, e.getMessage());
    }

    /**
     * 4003
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity unauthorizedHandle(UnauthorizedException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 6000
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity businessFailHandle(BusinessException e) {
        LOG.debug(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
    }

    /**
     * 9000
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResultEntity unknownHandle(Exception e) {
        LOG.error(e.getMessage(), e);
        return ResultEntity.faild(ResultCode.UNKNOWN_FAILED);
    }


    /**
     * JSR 303 Bean Validation MethodArgumentNotValidException 异常处理
     *
     * @param e
     */
    @ExceptionHandler
    public ResultEntity methodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult bs = e.getBindingResult();
        Map<String, String> errors = new HashMap<>(8);
        for (ObjectError objectError : bs.getAllErrors()) {
            FieldError fieldError = (FieldError) objectError;
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return illegalParamHandle(new IllegalParamException(errors.toString(), e));
    }

    /**
     * JSR 303 Bean Validation ConstraintViolationException 异常处理
     * Spring MVC 参数绑定 MissingServletRequestParameterException 异常处理
     *
     * @param e
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultEntity constraintViolation(Throwable e) {
        return illegalParamHandle(new IllegalParamException(e.getMessage(), e));
    }

    /**
     * * 6001  密码登录，密码过期需重置密码
     * @param e
     * @return
     */
    @ExceptionHandler(MdLoginPasswordTimeOutException.class)
    public ResultEntity LoginPasswordTimeOut(MdLoginPasswordTimeOutException e){
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setCode(6001);
        resultEntity.setMsg(e.getMessage());
        return resultEntity;

    }

    /**
     * * 第一次密码登录需要重置密码
     * @param e
     * @return
     */
    @ExceptionHandler(MdLoginPasswordFirstException.class)
    public ResultEntity LoginPasswordFist(MdLoginPasswordFirstException e) {
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setMsg(e.getMessage());
        resultEntity.setCode(6002);
        return resultEntity;
    }

    @ExceptionHandler(MdLoginErrorLockException.class)
    public ResultEntity LoginErrorLock(MdLoginErrorLockException e) {
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setMsg(e.getMessage());
        resultEntity.setCode(6003);
        return resultEntity;
    }

    @ExceptionHandler(MdLoginBlackUserException.class)
    public ResultEntity LoginBlackUser(MdLoginBlackUserException e) {
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setMsg(e.getMessage());
        resultEntity.setCode(7004);
        return resultEntity;
    }
}
