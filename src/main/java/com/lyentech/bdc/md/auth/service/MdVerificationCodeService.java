package com.lyentech.bdc.md.auth.service;

import org.springframework.validation.annotation.Validated;

/**
 * @author guolanren
 */
@Validated
public interface MdVerificationCodeService {

    /**
     * 验证码默认有效时间 5 分钟
     */
    Long DEFAULT_TIMEOUT_SECONDS = 5 * 60L;

    /**
     * 发送验证码
     *
     * @param address 手机、邮箱等
     */
    void send(String address, String scene) throws InterruptedException;

}
