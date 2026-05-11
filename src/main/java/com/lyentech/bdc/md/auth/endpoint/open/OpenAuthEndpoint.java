package com.lyentech.bdc.md.auth.endpoint.open;

import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;

import com.lyentech.bdc.md.auth.model.vo.AuthVO;
import com.lyentech.bdc.md.auth.model.vo.RoleDetailVO;
import com.lyentech.bdc.md.auth.service.AuthService;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.APP_KEY_PARAM;
import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.KEY_SIGN_PARAM;

/**
 * @Author :yan
 * @Date :Create in 2022/9/28
 * @Description :
 */

@RestController
@RequestMapping("/open/auth")
public class OpenAuthEndpoint {

    @Autowired
    private MdAppService appService;
    @Autowired
    private AuthService authService;

    @GetMapping("/getList")
    public ResultEntity getDetail(@RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, null, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            List<AuthVO> allAuth = authService.getAllAuth(appKey);
            return ResultEntity.success(allAuth);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }
}
