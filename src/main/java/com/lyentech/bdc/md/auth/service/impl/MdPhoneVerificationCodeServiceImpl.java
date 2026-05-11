package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lyentech.bdc.exception.BusinessException;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.kr.starter.constant.KrSmsType;
import com.lyentech.bdc.kr.starter.service.KrInfoPush;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.service.MdVerificationCodeService;
import com.lyentech.bdc.md.auth.util.RandomNumberStringGenerator;
import com.montnets.emp.im.api.APIClient;
import com.montnets.emp.im.api.impl.APIClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author guolanren
 */
@Service
@Slf4j
public class MdPhoneVerificationCodeServiceImpl implements MdVerificationCodeService {

    public static final String MD_AUTH_SCENE_VERIFICATION_PHONE_REDIS_KEY_PREFIX = "md:auth:%s:verification:phone_%s";
    public static final String SMS_CONTENT_TEMPLATE = "您的登陆验证码位：%s，该验证码5分钟有效，请勿泄露他人！";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MdUserMapper mdUserMapper;
    @Autowired
    private KrInfoPush krInfoPush;



    private RandomNumberStringGenerator generator = new RandomNumberStringGenerator();

    @Override
    public void send(String phone, String scene) throws InterruptedException {
        String phoneRegexp = "1[3456789]\\d{9}";
        if (phone.matches(phoneRegexp)) {
            if (scene.equals(KrSmsType.LOGIN)){
                MdUser mdUser = mdUserMapper.searchByPhone(phone);
                if (ObjectUtils.isEmpty(mdUser)) {
                    throw new IllegalParamException("用户不存在");
                }
            }
            doSend(phone, scene);
        } else {
            throw new IllegalParamException("手机号无效");
        }
    }

    private void doSend(String phone, String scene) throws InterruptedException {

        /**
         * 如果需要发送短信登陆验证码
         */
        RandomNumberStringGenerator generator = new RandomNumberStringGenerator();
        String param = generator.generate();
        log.info("验证码为：{}", param);
        krInfoPush.sendSmsCode(phone, param, KrSmsType.LOGIN);
        Thread.sleep(1000);
    }
}
