package com.lyentech.bdc.md.auth.tencent.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.lyentech.bdc.md.auth.model.param.MdAfsParam;
import com.lyentech.bdc.md.auth.tencent.exception.MdAfsException;
import com.lyentech.bdc.md.auth.tencent.properties.AfsProperties;
import com.tencentcloudapi.captcha.v20190722.CaptchaClient;
import com.tencentcloudapi.captcha.v20190722.models.DescribeCaptchaResultRequest;
import com.tencentcloudapi.captcha.v20190722.models.DescribeCaptchaResultResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

import static com.lyentech.bdc.md.auth.common.constant.MdAfsConstant.RAND_STR;
import static com.lyentech.bdc.md.auth.common.constant.MdAfsConstant.TICKET;
import static com.lyentech.bdc.md.auth.util.IpCheckRegionUtil.getIpRegion;

/**
 * 配置阿里云人机服务
 *
 * @author guolanren
 */
public class AfsService {

    public static final int AFS_SUCCESS = 1;
    public static final int TICKET_EXPIRED = 9;
    private static final Logger logger = LoggerFactory.getLogger(AfsService.class);
    private AfsProperties afsProperties;

    private CaptchaClient client;

    private Credential credential;
    private HttpProfile httpProfile;
    private ClientProfile clientProfile = new ClientProfile();


    public AfsService(AfsProperties afsProperties) {
        this.afsProperties = afsProperties;

        credential = new Credential(afsProperties.getSecretId(), afsProperties.getSecretKey());

        httpProfile = new HttpProfile();
        httpProfile.setEndpoint("captcha.tencentcloudapi.com");
        httpProfile.setProtocol(HttpProfile.REQ_HTTP);
        clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        client = new CaptchaClient(credential, "", clientProfile);
    }

    public Integer check(HttpServletRequest httpRequest) throws TencentCloudSDKException {
        String userIp = ServletUtil.getClientIP(httpRequest);
        Integer checkResultCode = 1;
        Boolean isOutWeb = getIpRegion(userIp);
        if (isOutWeb == false) {
            logger.info("用户IP地址：{},用户是内网IP不需要人机",userIp);
            checkResultCode = 1;
            return checkResultCode;
        }
        logger.info("用户IP地址：{},用户是外网IP需要人机",userIp);
        MdAfsParam afsParam = obtainAfsParam(httpRequest);
        DescribeCaptchaResultRequest request = new DescribeCaptchaResultRequest();
        request.setCaptchaType(afsProperties.getCaptchaType());
        request.setUserIp(afsProperties.getUserIp());
        request.setCaptchaAppId(afsProperties.getCaptchaAppId());
        request.setAppSecretKey(afsProperties.getAppSecretKey());
        request.setRandstr(afsParam.getRandStr());
        request.setTicket(afsParam.getTicket());
        DescribeCaptchaResultResponse response = client.DescribeCaptchaResult(request);
        logger.info("人机验证结果码：" + response.getCaptchaCode()+"结果消息"+response.getCaptchaMsg());
        if (response.getCaptchaCode() != AFS_SUCCESS) {
            logger.info("afs验证失败[{}]：{}", response.getCaptchaCode(), response.getCaptchaMsg());
            checkResultCode = response.getCaptchaCode().intValue();
        } else {
            checkResultCode = 1;
        }
        return checkResultCode;
//        return AFS_SUCCESS;
    }

    private MdAfsParam obtainAfsParam(HttpServletRequest request) throws RuntimeException {
        String ticket = request.getParameter(TICKET);
        Assert.notNull(ticket, "afs 参数 ticket 缺失");

        String randStr = request.getParameter(RAND_STR);
        Assert.notNull(randStr, "afs 参数 randStr 缺失");


        return new MdAfsParam(ticket, randStr);
    }
}
