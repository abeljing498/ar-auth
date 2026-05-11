package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.service.impl.SendMessageToWebSocketServiceFallBackImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "ar-ws-server", fallback = SendMessageToWebSocketServiceFallBackImpl.class)
public interface SendMessageToWebSocketService {
    /**
     * 角色变更
     * @param pushMessageDto
     * @return
     */
    @PostMapping(value = "/auth/message/pushMessage")
    ResultEntity sendMessage( @RequestBody PushMessageDto pushMessageDto);
    /**
     * 强制用户等处指令
     * @param pushAppOrderMsgDto
     * @return
     */
    @PostMapping(value = "/auth/message/pushAppOrderMessage")
    ResultEntity pushAppOrderMessage(@RequestBody PushAppOrderMsgDto pushAppOrderMsgDto);

}
