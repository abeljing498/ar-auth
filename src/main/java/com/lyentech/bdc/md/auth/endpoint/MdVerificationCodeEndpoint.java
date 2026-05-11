package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.verification.VerificationType;
import com.lyentech.bdc.md.auth.model.param.MdVerificationCodeParam;
import com.lyentech.bdc.md.auth.service.MdVerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guolanren
 */
@RestController
@Validated
public class MdVerificationCodeEndpoint {

    @Autowired
    private MdVerificationCodeService phoneVerificationCodeService;

    /**
     * 调用接口，发送验证
     *
     * @param verificationCodeParam 手机号/邮箱（暂不支持）
     * @return
     */
    @PostMapping("/verification_code")
    public ResultEntity code(@RequestBody MdVerificationCodeParam verificationCodeParam) throws InterruptedException {
        String address = verificationCodeParam.getAddress();
        String codeType = verificationCodeParam.getType();
        String scene = verificationCodeParam.getUseTo();

        switch (VerificationType.valueOf(codeType.toUpperCase())) {
            case EMAIL:
                break;
            case PHONE:
                phoneVerificationCodeService.send(address, scene);
                break;
            default:
                throw new IllegalParamException("不支持的验证码类型：" + codeType + "。支持的类型有：phone|email");
        }
        return ResultEntity.success();
    }

}
