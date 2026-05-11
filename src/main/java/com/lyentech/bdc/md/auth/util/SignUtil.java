package com.lyentech.bdc.md.auth.util;

import com.lyentech.bdc.exception.IllegalParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author :yan
 * @Date :Create in 2022/8/31
 * @Description :
 */

public class SignUtil {

    private static final String KEY_SIGN_PARAM = "signature";
    private static final String TIMESTAMP_PARAM = "timestamp";
    private static Logger logger = LoggerFactory.getLogger(SignUtil.class);

    /**
     * 校验签名的时效性，并生成签名（该方法仅接口提供方用，接口调用方不需要加时间校验）
     *
     * @param params   请求中query params
     * @param postData 请求Body的JsonString
     * @param secret   系统颁发的secret
     * @return sign签名串
     */
    public static String sign(Map<String, String> params, String postData, String secret) throws UnsupportedEncodingException {


        // 校验时间戳是否为10分钟内
        Long now = System.currentTimeMillis();
        Long diff =  now - Long.valueOf(params.get(TIMESTAMP_PARAM));
        Long minValidTime = 60000L;
        Long maxValidTime = 600000L;
        if ((diff >= 0L || Math.abs(diff) <= minValidTime) && diff <= maxValidTime) {
            StringBuilder signEntity = new StringBuilder();
            // 第一步：把参数排序后并把所有参数名和参数值串在一起
            if (!params.isEmpty()) {
                signEntity.append(params.keySet().stream().sorted().filter(key -> !KEY_SIGN_PARAM.equals(key))
                        .map(key -> key + params.get(key)).collect(Collectors.joining()));
            }
            // 第二步：拼接post消息体
            if (!StringUtils.isEmpty(postData)) {
                signEntity.append(postData);
            }
            // 第三步：附加secret
            signEntity.append(secret);

            // 第四步：md5签名
            return DigestUtils.md5DigestAsHex(signEntity.toString().getBytes("UTF-8")).toUpperCase();
        } else {
            logger.info("请求时间戳{}，现在时间戳{}，时间戳差{}",params.get(TIMESTAMP_PARAM), now, diff);
            throw new IllegalParamException("签名已过期");
        }
    }


}
