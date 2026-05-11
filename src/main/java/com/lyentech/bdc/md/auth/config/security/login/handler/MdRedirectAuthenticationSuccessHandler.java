package com.lyentech.bdc.md.auth.config.security.login.handler;

import cn.hutool.extra.servlet.ServletUtil;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.constant.MdLoginAuthTypeConstant;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdLoginParam;
import com.lyentech.bdc.md.auth.util.HttpResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.CHEMICAL_PLANT_URL;
import static com.lyentech.bdc.md.auth.common.constant.MdLoginProcessFilterConstant.MD_LOGIN_BODY;

/**
 * @author guolanren
 */
@Slf4j
public class MdRedirectAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private MdLoginLogMapper loginLogMapper;

    public MdRedirectAuthenticationSuccessHandler(MdLoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        MdLoginParam loginParam = (MdLoginParam) request.getAttribute(MD_LOGIN_BODY);
        String defaultReturnUri = (String) request.getAttribute(CHEMICAL_PLANT_URL);
        String forwardUri = defaultReturnUri;

        // 如果没有想去的地方，就去化学工厂看看吧
        if (loginParam != null && loginParam.getReturnTo() != null) {
            forwardUri = URLDecoder.decode(loginParam.getReturnTo(), "UTF-8");
        }
        //forwardUri.replaceAll("http://ar-dev.leayun.cn","http://ar-pre.leayun.cn");

        log.info("认证成功跳转地址：{}", forwardUri);
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(forwardUri).build();
        List<String> clients = uriComponents.getQueryParams().get("client_id");
        String clientId = clients.get(0);
        MdUser mdUser = (MdUser) authentication.getPrincipal();
        ResultEntity resultEntity = ResultEntity.forward(forwardUri);
        HttpResponseUtil.setResultEntityAsContent(response, resultEntity);

        MdLoginLog loginLog = new MdLoginLog();
        loginLog.setUserId(mdUser.getId());
        if (MdLoginAuthTypeConstant.OUTAUTH.equals(loginParam.getAuthType())) {
            loginLog.setLoginWay("smsAuth");
        }else{
            loginLog.setLoginWay(loginParam.getAuthType());
        }

        loginLog.setOperateSystem(loginParam.getLoginSystem());
        loginLog.setBrowser(loginParam.getLoginBrowser());
        loginLog.setIp(ServletUtil.getClientIP(request));
        loginLog.setAppId(clientId);
        loginLogMapper.insert(loginLog);

        clearAuthenticationAttributes(request);
    }
}
