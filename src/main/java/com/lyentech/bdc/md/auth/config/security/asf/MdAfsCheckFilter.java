package com.lyentech.bdc.md.auth.config.security.asf;

import com.lyentech.bdc.http.response.ResultCode;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdAfsCheckException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginPasswordErrorException;
import com.lyentech.bdc.md.auth.config.security.login.filter.AbstractMdSecurityFilter;
import com.lyentech.bdc.md.auth.model.param.MdAfsParam;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import com.lyentech.bdc.md.auth.tencent.exception.MdAfsException;
import com.lyentech.bdc.md.auth.tencent.service.AfsService;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.lyentech.bdc.md.auth.common.constant.MdAfsConstant.*;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant.PASSWORD;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.MD_LOGIN_BODY;
import static com.lyentech.bdc.md.auth.tencent.service.AfsService.AFS_SUCCESS;
import static com.lyentech.bdc.md.auth.tencent.service.AfsService.TICKET_EXPIRED;

/**
 * 人机验证拦截器
 *
 * @author guolanren
 */
public class MdAfsCheckFilter extends AbstractMdSecurityFilter {

    private AfsService afsService;

    public MdAfsCheckFilter(AfsService afsService, String... antPatterns) throws Exception {
        super(HttpMethod.POST, antPatterns);
        this.afsService = afsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Boolean skipAfs = (Boolean) request.getAttribute(SKIP_AFS);
        if (skipAfs != null && skipAfs) {
            chain.doFilter(request, response);
            return;
        }
        if (!requiresAuthentication(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Integer isCheck = afsService.check(httpRequest);
            if (isCheck != AFS_SUCCESS) {
                ResultEntity resultEntity = ResultEntity.faild(4005, "", "滑块验证码已失效，请重新验证！");
                HttpResponseUtil.setResultEntityAsContent(httpResponse, resultEntity);
                return;

            }
        } catch (IllegalArgumentException e) {
            ResultEntity resultEntity = ResultEntity.faild(ResultCode.ILLEGAL_PARAM_FAILED, e.getMessage());
            HttpResponseUtil.setResultEntityAsContent(httpResponse, resultEntity);
            return;
        } catch (MdAfsException e) {
            ResultEntity resultEntity = ResultEntity.faild(ResultCode.BUSINESS_FAIL, e.getMessage());
            HttpResponseUtil.setResultEntityAsContent(httpResponse, resultEntity);
            return;
        } catch (TencentCloudSDKException e) {
            ResultEntity resultEntity = ResultEntity.faild(ResultCode.SERVER_ERROR, e.getMessage());
            HttpResponseUtil.setResultEntityAsContent(httpResponse, resultEntity);
            return;
        }
        chain.doFilter(request, response);
    }

    private MdAfsParam obtainAfsParam(HttpServletRequest request) throws RuntimeException {
        String ticket = request.getParameter(TICKET);
        Assert.notNull(ticket, "afs 参数 ticket 缺失");

        String randStr = request.getParameter(RAND_STR);
        Assert.notNull(randStr, "afs 参数 randStr 缺失");


        return new MdAfsParam(ticket, randStr);
    }

    protected MdLoginParam obtainLoginParam(HttpServletRequest request) {
        MdLoginParam loginParam = (MdLoginParam) request.getAttribute(MD_LOGIN_BODY);
        return loginParam;
    }
}
