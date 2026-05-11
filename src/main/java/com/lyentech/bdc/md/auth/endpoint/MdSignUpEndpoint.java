package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.MdJoinParam;
import com.lyentech.bdc.md.auth.service.MdUserService;
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
public class MdSignUpEndpoint {

    @Autowired
    private MdUserService userService;

    @Autowired
    private MdVerificationCodeService phoneVerificationCodeService;

    /**
     * 账号注册，返回用户ID。
     * 提供用户名、手机号、密码（如果有加密规则，请告知），如果已存在请返回用户ID。
     * tips：在删除账号相关信息时，会删除相关的角色信息，而重新加回来后，并不会再恢复
     */
    @PostMapping("/join")
    public ResultEntity join(@RequestBody @Validated MdJoinParam joinParam) {
        MdUser user = userService.register(joinParam);
        return ResultEntity.success(user.getId());
    }
}
