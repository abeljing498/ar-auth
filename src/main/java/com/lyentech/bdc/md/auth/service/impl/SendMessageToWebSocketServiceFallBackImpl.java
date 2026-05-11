package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
/**
 * 服务异常进行降级处理
 */
@Slf4j
public class SendMessageToWebSocketServiceFallBackImpl implements SendMessageToWebSocketService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public ResultEntity sendMessage(PushMessageDto pushMessageDto) {
        //消息暂存Redis
        if (ObjectUtils.isNotEmpty(pushMessageDto)) {
            stringRedisTemplate.opsForHash().putIfAbsent("ar-roles:" + pushMessageDto.getAppKey().trim(), pushMessageDto.getTenantId() + "@" + pushMessageDto.getRoleId() + "@" + pushMessageDto.getStatus(), JSON.toJSONString(pushMessageDto));
        }
        log.error("访问频繁或请求服务异常");
        return ResultEntity.faild(HttpStatus.TOO_MANY_REQUESTS.value(),null,"访问频繁或请求服务异常！");
    }


    @Override
    public ResultEntity pushAppOrderMessage(PushAppOrderMsgDto pushAppOrderMsgDto) {
        log.error("向下游系统推送消息访问异常！{}",JSON.toJSONString(pushAppOrderMsgDto));
        return null;
    }
}
